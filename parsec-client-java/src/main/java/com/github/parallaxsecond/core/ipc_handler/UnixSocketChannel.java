package com.github.parallaxsecond.core.ipc_handler;

import com.github.parallaxsecond.exceptions.InvalidSocketAddressException;
import com.github.parallaxsecond.exceptions.IpcException;
import com.sun.jna.*;
import jnr.posix.POSIXFactory;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class UnixSocketChannel implements ByteChannel {

  interface Socket extends Library {

    class SockAddrUn extends Structure {

      public static final List<String> FIELDS = createFieldsOrder("sunFamily", "path");
      public short sunFamily = AF_UNIX;
      public byte[] path = new byte[92];

      @Override
      protected List<String> getFieldOrder() {
        return FIELDS;
      }
    }
    ;

    // these are the same for mac and linux
    short AF_UNIX = 1;
    short SOCK_STREAM = 1;

    Socket SOCKET_IMPL = Native.load("c", Socket.class);

    int socket(int domain, int type, int protocol);

    String strerror(int errno);

    int connect(int socket, Pointer addr, int size);

    int close(int socket);

    long read(int socket, Pointer pointer, long size);

    long write(int socket, Pointer pointer, long size);

    static void throwError(long ret) {
      if (ret == -1) {
        int errno = Native.getLastError();
        String error = SOCKET_IMPL.strerror(errno);
        throw new IpcException(error);
      }
    }

    static int unixSocket() {
      int socket = SOCKET_IMPL.socket(AF_UNIX, SOCK_STREAM, 0);
      throwError(socket);
      return socket;
    }

    static void connect(int socket, @NonNull String path) {
      if (path.length() >= 92) {
        throw new IpcException(path + " exceeds 92 characters");
      }

      SockAddrUn addr = new SockAddrUn();
      byte[] pathArray = Native.toByteArray(path, StandardCharsets.UTF_8);
      System.arraycopy(pathArray, 0, addr.path, 0, pathArray.length);
      addr.write();
      int ret = SOCKET_IMPL.connect(socket, addr.getPointer(), addr.size());
      throwError(ret);
    }

    static void closeSocket(int socket) {
      int ret = SOCKET_IMPL.close(socket);
      throwError(ret);
    }

    static long readSocket(int socket, Memory buf, int size) {
      long ret = SOCKET_IMPL.read(socket, buf, Math.min(size, buf.size()));
      throwError(ret);
      return ret;
    }

    static long writeSocket(int socket, Memory buf, int size) {
      long ret = SOCKET_IMPL.write(socket, buf, Math.min(size, buf.size()));
      throwError(ret);
      return ret;
    }
  }

  private final Memory readBuffer = new Memory(1024);
  private final Memory writeBuffer = new Memory(1024);

  private volatile boolean open;
  private final int socket;

  public UnixSocketChannel(@NonNull Path path) {

    if (!Files.exists(path) || !POSIXFactory.getNativePOSIX().stat(path.toString()).isSocket()) {
      throw new InvalidSocketAddressException(path);
    }

    this.socket = Socket.unixSocket();
    Socket.connect(this.socket, path.toString());

    this.open = true;
  }

  @Override
  public int read(ByteBuffer dst) {
    int size = (int) Socket.readSocket(this.socket, readBuffer, dst.remaining());
    dst.put(readBuffer.getByteBuffer(0, size));
    return size;
  }

  @Override
  public int write(ByteBuffer src) {
    int pos = src.position();
    ByteBuffer bb = writeBuffer.getByteBuffer(0, writeBuffer.size());
    bb.put(src);
    bb.flip();
    src.position(pos);
    int size = (int) Socket.writeSocket(socket, writeBuffer, bb.remaining());
    if (size > 0) {
      src.position(pos + size);
    }
    return size;
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public void close() {
    Socket.closeSocket(this.socket);
    open = false;
  }
}
