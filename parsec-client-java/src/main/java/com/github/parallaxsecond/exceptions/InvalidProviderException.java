package com.github.parallaxsecond.exceptions;

/** The operation is not supported by the selected provider */
public class InvalidProviderException extends ClientException {
  public InvalidProviderException(Exception e) {
    super("operation not supported by selected provider", e);
  }
}
