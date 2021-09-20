package com.github.parallaxsecond.exceptions;

/** Client is missing an implicit provider */
public class NoProviderException extends ClientException {
  public NoProviderException() {
    this(null);
  }

  public NoProviderException(Exception e) {
    super("client is missing an implicit provider", e);
  }
}
