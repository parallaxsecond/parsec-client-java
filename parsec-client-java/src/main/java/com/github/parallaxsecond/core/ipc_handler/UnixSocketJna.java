package com.github.parallaxsecond.core.ipc_handler;

import com.github.parallaxsecond.Platform;
import com.github.parallaxsecond.exceptions.IpcException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

interface UnixSocketJna extends Library {
  Logger log = LoggerFactory.getLogger(UnixSocketJna.class);
  // these are the same for mac and linux
  int AF_UNIX = 1;
  int SOCK_STREAM = 1;
  int SOL_SOCKET = Platform.value().osx(0xffff).linux(1).get();
  int SO_RCVTIMEO = Platform.value().osx(0x1006).linux(20).get();
  int SO_SNDTIMEO = Platform.value().osx(0x1005).linux(21).get();

  UnixSocketJna SOCKET_IMPL = Native.load("c", UnixSocketJna.class);

  static void throwError(long ret) {
    if (ret < 0) {
      int errno = Native.getLastError();
      String error = SOCKET_IMPL.strerror(errno);
      throw new IpcException(error);
    }
  }

  static void warnError(long ret) {
    if (ret < 0) {
      int errno = Native.getLastError();
      log.warn(SOCKET_IMPL.strerror(errno));
    }
  }

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

  static void connect(int socket, @NonNull String path) {
    if (path.getBytes(StandardCharsets.UTF_8).length >= 92) {
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

  String strerror(int errno);

  int connect(int socket, Pointer addr, int size);

  int close(int socket);

  long read(int socket, ByteBuffer pointer, long size);

  long write(int socket, ByteBuffer pointer, long size);

  int setsockopt(int socket, int level, int option_name, Pointer optionValue, long option_len);

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
    public long tvSec = 0;
    public long tvUSec = 0;

    public Timeval(Duration duration) {
      tvSec = duration.getSeconds();
      tvUSec = duration.getNano() / 1000L;
    }

    @Override
    protected List<String> getFieldOrder() {
      return FIELDS;
    }
  }
  ;
}
