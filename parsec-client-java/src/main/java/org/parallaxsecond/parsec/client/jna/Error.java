package org.parallaxsecond.parsec.client.jna;

import org.parallaxsecond.parsec.client.exceptions.IpcException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Error extends Library {
  Logger log = LoggerFactory.getLogger(Error.class);
  Error IMPL = Native.load("c", Error.class);

  static void throwError(long ret) {
    if (ret < 0) {
      int errno = Native.getLastError();
      String error = IMPL.strerror(errno);
      throw new IpcException(error);
    }
  }

  static void warnError(long ret) {
    if (ret < 0) {
      int errno = Native.getLastError();
      if (log.isWarnEnabled()) {
        log.warn(IMPL.strerror(errno));
      }
    }
  }

  String strerror(int errno);
}
