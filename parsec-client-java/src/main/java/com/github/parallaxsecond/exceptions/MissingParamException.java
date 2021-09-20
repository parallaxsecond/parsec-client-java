package com.github.parallaxsecond.exceptions;

/** Required parameter was not provided */
public class MissingParamException extends ClientException {
  public MissingParamException() {
    this(null);
  }

  public MissingParamException(Exception e) {
    super("one of the `Option` parameters was required but was not provided", e);
  }
}
