package com.github.parallaxsecond.core.ipc_handler;

import com.github.parallaxsecond.exceptions.InvalidSocketAddressException;
import jnr.posix.POSIXFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

@Slf4j
public class UnixSocketChannel implements ByteChannel {

  private volatile boolean open;
  private final int socket;

  public UnixSocketChannel(@NonNull Path path, Duration timeout) {
    if (!Files.exists(path) || !POSIXFactory.getNativePOSIX().stat(path.toString()).isSocket()) {
      throw new InvalidSocketAddressException(path);
    }
    this.socket = UnixSocketJna.unixSocket();
    UnixSocketJna.connect(this.socket, path.toString());
    UnixSocketJna.setReceiveTimeout(this.socket, timeout);
    UnixSocketJna.setSendTimeout(this.socket, timeout);
    this.open = true;
  }

  @Override
  public int read(ByteBuffer dst) {
    int toRead = dst.remaining();
    int read = 0;
    int pos = dst.position();
    while (read < toRead) {
      read += (int) UnixSocketJna.readSocket(this.socket, dst, toRead - read);
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
      written += (int) UnixSocketJna.writeSocket(socket, src, toWrite - written);
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
    UnixSocketJna.closeSocket(this.socket);
    open = false;
  }
}
