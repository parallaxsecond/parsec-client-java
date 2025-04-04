package org.parallaxsecond.testcontainers;

import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import static java.util.Collections.singletonList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Assertions;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.ShellStrategy;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParsecContainer extends GenericContainer<ParsecContainer> {
  private static final String IMAGE_NAME = "parallaxsecond/parsec";
  private static final String PARSEC_SOCKET_FILE = "parsec.sock";
  private static final String PARSEC_RUN_DIR = "/parsec/quickstart/";
  private static final String PARSEC_SOCKET = PARSEC_RUN_DIR + PARSEC_SOCKET_FILE;
  private static final int PARSEC_TCP_PORT = 4444;
  private final int localPort = findFreePort();
  private final Path parsecSockSocat;
  private final Path parsecSock;
  private Process cmd;
  private ExecutorService executor;

  public Path runDir;

  @SneakyThrows
  public ParsecContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    this.setWaitStrategy(new ShellStrategy().withCommand("parsec-tool ping")
        .withStartupTimeout(Duration.ofSeconds(10)));
    runDir = Files.createTempDirectory("parsec_");
    Files.setPosixFilePermissions(runDir, PosixFilePermissions.fromString("rwxrwxrwx"));
    this.parsecSock = runDir.resolve(PARSEC_SOCKET_FILE);
    this.parsecSockSocat = runDir.resolve("ps_socat.sock");

    this.setPortBindings(singletonList(localPort + ":" + PARSEC_TCP_PORT));
    this.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(ParsecContainer.class)));
  }

  public boolean isRunning() {
    if (getContainerId() == null) {
      return false;
    }

    try {
      Boolean running = getCurrentContainerInfo().getState().getRunning();
      return Boolean.TRUE.equals(running);
    } catch (Throwable e) {
      return false;
    }
  }

  @SneakyThrows
  public static ParsecContainer withVersion(String version) {
    ParsecContainer parsecContainer =
        new ParsecContainer(DockerImageName.parse(IMAGE_NAME + ":" + version));

    parsecContainer.start();

    // Wait for container to be running
    Awaitility.await().until(parsecContainer::isRunning);


    return parsecContainer;
  }

  @SneakyThrows
  private static int findFreePort() {
    try (ServerSocket serverSocket = new ServerSocket(0)) {
      return serverSocket.getLocalPort();
    }
  }

  public URI getSocketUri() {
    Path sock = this.parsecSock;
    if (Files.exists(this.parsecSockSocat)) {
      sock = this.parsecSockSocat;
    }
    return URI.create("unix:" + sock.toUri().getPath());
  }

  @Override
  public void start() {
    if (isOsx()) {
      useSocat();
    } else {
    }
    super.start();
  }

  private static boolean isOsx() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return ((os.contains("mac")) || (os.contains("darwin")));
  }

  @Override
  public void close() {
    if (this.cmd != null) {
      this.cmd.destroyForcibly();
      this.cmd = null;
    }
    if (this.executor != null) {
      this.executor.shutdownNow();
      this.executor = null;
    }
  }

  @SneakyThrows
  public void useSocat() {
    executor = Executors.newSingleThreadExecutor();
    executor.submit(() -> {
      ParsecContainer.this.execInContainer("socat", "-d", "-d", // Add debug output
          "TCP4-LISTEN:" + PARSEC_TCP_PORT + ",reuseaddr,fork", "UNIX-CONNECT:" + PARSEC_SOCKET);
      return null;
    });

    cmd = Runtime.getRuntime().exec(new String[] {"socat", "-d", "-d", // Add debug output
        "UNIX-LISTEN:" + this.parsecSockSocat.toFile().getAbsolutePath() + ",fork,reuseaddr",
        "TCP4:localhost:" + this.localPort});

    // wait until this.parsecSockSocat is created
    Awaitility.await().until(() -> {
      return Files.exists(this.parsecSockSocat);
    });

  }

  @SneakyThrows
  public void parsecTool(String... args) {
    String[] args_ = new String[args.length + 1];
    System.arraycopy(args, 0, args_, 1, args.length);
    args_[0] = "parsec-tool";
    ExecResult r = execInContainer(args_);
    Assertions.assertEquals(0, r.getExitCode(),
        getLogs() + "\n" + r.getStdout() + "\n" + r.getStderr());
  }
}
