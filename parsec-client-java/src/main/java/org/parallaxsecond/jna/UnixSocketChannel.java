package org.parallaxsecond.jna;

import org.parallaxsecond.exceptions.InvalidSocketAddressException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class UnixSocketChannel implements ByteChannel {

  private final int socket;
  private volatile boolean open;

  public UnixSocketChannel(@NonNull Path path, Duration timeout) {
    if (!Files.exists(path) || !FileStat.isSocket(path)) {
      throw new InvalidSocketAddressException(path);
    }
    this.socket = UnixSocket.unixSocket();
    UnixSocket.connect(this.socket, path.toString());
    UnixSocket.setReceiveTimeout(this.socket, timeout);
    UnixSocket.setSendTimeout(this.socket, timeout);
    this.open = true;
  }

  @Override
  public int read(ByteBuffer dst) {
    int toRead = dst.remaining();
    int read = 0;
    int pos = dst.position();
    while (read < toRead) {
      read += (int) UnixSocket.readSocket(this.socket, dst, toRead - read);
      log.debug("expected: {}, read: {}", toRead, read);
      dst.position(pos + read);
    }
    return toRead;
  }

  @Override
  public int write(ByteBuffer src) {
    int toWrite = src.remaining();
    int written = 0;
    int pos = src.position();
    while (written < toWrite) {
      written += (int) UnixSocket.writeSocket(socket, src, toWrite - written);
      src.position(pos + written);
    }
    return written;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void close() {
    UnixSocket.closeSocket(this.socket);
    open = false;
  }
}
