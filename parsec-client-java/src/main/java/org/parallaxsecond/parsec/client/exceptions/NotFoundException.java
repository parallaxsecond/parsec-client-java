package org.parallaxsecond.parsec.client.exceptions;

/** The requested resource was not found. */
public class NotFoundException extends ClientException {
  public NotFoundException() {
    this(null);
  }

  public NotFoundException(Exception e) {
    super("one of the resources required in the operation was not found", e);
  }
}
