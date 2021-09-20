package com.github.parallaxsecond.exceptions;

import java.nio.file.Path;

/** The socket address provided is not valid */
public class InvalidSocketAddressException extends ClientException {
  public InvalidSocketAddressException(Path path) {
    super("the socket address provided in the URL is not valid: " + path, null);
  }
}
