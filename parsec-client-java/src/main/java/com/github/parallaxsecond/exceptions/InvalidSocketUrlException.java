package com.github.parallaxsecond.exceptions;

import java.net.URI;

/** The socket URL is invalid */
public class InvalidSocketUrlException extends ClientException {
  public InvalidSocketUrlException(URI uri) {
    super("the socket URL is invalid: " + uri, null);
  }
}
