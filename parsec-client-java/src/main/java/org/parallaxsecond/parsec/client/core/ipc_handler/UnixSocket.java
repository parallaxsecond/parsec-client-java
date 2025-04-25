package org.parallaxsecond.parsec.client.core.ipc_handler;

import java.nio.channels.ByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Objects;

import org.parallaxsecond.parsec.client.jna.UnixSocketChannel;

import lombok.Setter;

/** IPC handler for Unix domain sockets */
public class UnixSocket implements IpcHandler {
  /** Default socket path used by the service. */
  public static final String DEFAULT_SOCKET_PATH = "/run/parsec/parsec.sock";
  /** Path at which the socket can be found */
  private final Path path;
  /** Timeout for reads and writes on the streams */
  @Setter
  private Duration timeout = DEFAULT_TIMEOUT;

  public UnixSocket(String path) {
    this(path, DEFAULT_TIMEOUT);
  }

  public UnixSocket(String path, Duration timeout) {
    Objects.requireNonNull(path);
    this.path = Paths.get(path);
    this.timeout = timeout;
  }

  public static UnixSocket withDefaults() {
    return new UnixSocket(DEFAULT_SOCKET_PATH, DEFAULT_TIMEOUT);
  }

  @Override
  public ByteChannel connect() {
    return new UnixSocketChannel(path, timeout);
  }
}
