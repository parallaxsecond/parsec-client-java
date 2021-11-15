package org.parallaxsecond.parsec.client.exceptions;
/** Types of errors local to the client library */
public class ClientException extends RuntimeException {
  public ClientException(String message, Exception e) {
    super(message, e);
  }
}
