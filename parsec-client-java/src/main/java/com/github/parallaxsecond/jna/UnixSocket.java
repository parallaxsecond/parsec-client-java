package com.github.parallaxsecond.jna;

import com.github.parallaxsecond.exceptions.IpcException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static com.github.parallaxsecond.jna.Error.throwError;
import static com.github.parallaxsecond.jna.Error.warnError;

interface UnixSocket extends Library {
  // these are the same for mac and linux
  int AF_UNIX = 1;
  int SOCK_STREAM = 1;
  int SOL_SOCKET = Platform.value().osx(0xffff).linux(1).get();
  int SO_RCVTIMEO = Platform.value().osx(0x1006).linux(20).get();
  int SO_SNDTIMEO = Platform.value().osx(0x1005).linux(21).get();

  UnixSocket SOCKET_IMPL = Native.load("c", UnixSocket.class);

  static int setTimeout(int socket, Duration timeout, int type) {
    Timeval tv = new Timeval(timeout);
    tv.write();
    int ret = SOCKET_IMPL.setsockopt(socket, SOL_SOCKET, type, tv.getPointer(), tv.size());
    warnError(ret);
    return ret;
  }

  static int setReceiveTimeout(int socket, Duration timeout) {
    return setTimeout(socket, timeout, SO_RCVTIMEO);
  }

  static int setSendTimeout(int socket, Duration timeout) {
    return setTimeout(socket, timeout, SO_SNDTIMEO);
  }

  static int unixSocket() {
    int socket = SOCKET_IMPL.socket(AF_UNIX, SOCK_STREAM, 0);
    throwError(socket);
    return socket;
  }

  static void socketCall(SocketCall method, int socket, @NonNull String path) {
    if (path.getBytes(StandardCharsets.UTF_8).length >= 92) {
      throw new IpcException(path + " exceeds 92 characters");
    }
    SockAddrUn addr = new SockAddrUn();
    byte[] pathArray = Native.toByteArray(path, StandardCharsets.UTF_8);
    System.arraycopy(pathArray, 0, addr.path, 0, pathArray.length);
    addr.write();
    int ret = method.call(socket, addr.getPointer(), addr.size());
    throwError(ret);
  }

  static void bind(int socket, @NonNull String path) {
    socketCall(SOCKET_IMPL::bind, socket, path);
  }

  static void connect(int socket, @NonNull String path) {
    socketCall(SOCKET_IMPL::connect, socket, path);
  }

  static void closeSocket(int socket) {
    int ret = SOCKET_IMPL.close(socket);
    throwError(ret);
  }

  static long readSocket(int socket, ByteBuffer buf, int size) {
    long ret = SOCKET_IMPL.read(socket, buf, size);
    throwError(ret);
    return ret;
  }

  static long writeSocket(int socket, ByteBuffer buf, int size) {
    long ret = SOCKET_IMPL.write(socket, buf, size);
    throwError(ret);
    return ret;
  }

  int socket(int domain, int type, int protocol);

  int connect(int socket, Pointer addr, int size);

  int bind(int socket, Pointer addr, int size);

  int close(int socket);

  long read(int socket, ByteBuffer pointer, long size);

  long write(int socket, ByteBuffer pointer, long size);

  int setsockopt(int socket, int level, int optionName, Pointer optionValue, long optionLen);

  interface SocketCall {
    int call(int socket, Pointer p, int size);
  }

  @EqualsAndHashCode(callSuper = false)
  class SockAddrUn extends Structure {
    protected static final List<String> FIELDS = createFieldsOrder("sunFamily", "path");
    public short sunFamily = AF_UNIX;
    public byte[] path = new byte[92];

    @Override
    protected List<String> getFieldOrder() {
      return FIELDS;
    }
  }

  class Timeval extends Structure {
    protected static final List<String> FIELDS = createFieldsOrder("tvSec", "tvUSec");
    public long tvSec;
    public long tvUSec;

    public Timeval(Duration duration) {
      tvSec = duration.getSeconds();
      tvUSec = duration.getNano() / 1000L;
    }

    @Override
    protected List<String> getFieldOrder() {
      return FIELDS;
    }
  }
}
