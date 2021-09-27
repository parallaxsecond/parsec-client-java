package com.github.parallaxsecond;

import com.github.parallaxsecond.jna.Platform;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class ParsecContainer extends GenericContainer<ParsecContainer> {
  private static final String IMAGE_NAME = "parallaxsecond/parsec";
  private static final String PARSEC_SOCKET_FILE = "parsec.sock";
  private static final String PARSEC_RUN_DIR = "/run/parsec/";
  private static final String PARSEC_SOCKET = PARSEC_RUN_DIR + PARSEC_SOCKET_FILE;
  private static final int PARSEC_TCP_PORT = 4444;
  private final int localPort = findFreePort();
  private final Path parsecSockSocat;
  private final Path parsecSock;
  private Process cmd;
  private ExecutorService executor;

  @SneakyThrows
  public ParsecContainer(final DockerImageName dockerImageName) {
    super(dockerImageName);
    Path runDir = Files.createTempDirectory("ps");
    Files.setPosixFilePermissions(runDir, PosixFilePermissions.fromString("rwxrwxrwx"));
    this.parsecSock = runDir.resolve(PARSEC_SOCKET_FILE);
    this.parsecSockSocat = runDir.resolve("ps_socat.sock");
    this.withFileSystemBind(
        runDir.toFile().getAbsoluteFile().getAbsolutePath(), PARSEC_RUN_DIR, BindMode.READ_WRITE);
    this.setPortBindings(singletonList(localPort + ":" + PARSEC_TCP_PORT));
  }

  public static ParsecContainer withVersion(String version) {
    return new ParsecContainer(DockerImageName.parse(IMAGE_NAME + ":" + version));
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
    super.start();
    if (Platform.isOsx()) {
      useSocat();
    }
  }

  @Override
  public void close() {
    super.close();
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
    executor.submit(
        () -> {
          log.info("starting socat in docker container");
          ParsecContainer.this.execInContainer(
              "socat",
              "TCP4-LISTEN:" + PARSEC_TCP_PORT + ",reuseaddr,fork",
              "UNIX-CONNECT:" + PARSEC_SOCKET);
          log.info("started socat in docker container");
          return null;
        });

    log.info("starting socat on local machine");
    cmd =
        Runtime.getRuntime()
            .exec(
                new String[] {
                  "socat",
                  "UNIX-LISTEN:"
                      + this.parsecSockSocat.toFile().getAbsolutePath()
                      + ",fork,reuseaddr",
                  "TCP4:localhost:" + this.localPort
                });
    log.info("started socat on local machine");
  }

  @SneakyThrows
  public void parsecTool(String... args) {
    String[] args_ = new String[args.length + 1];
    System.arraycopy(args, 0, args_, 1, args.length);
    args_[0] = "parsec-tool";
    ExecResult r = execInContainer(args_);
    assertEquals(0, r.getExitCode(), getLogs() + "\n" + r.getStdout() + "\n" + r.getStderr());
  }
}
