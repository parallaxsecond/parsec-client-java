package com.github.parallaxsecond.exceptions;
/** Service is missing authenticator or none of the authenticators is supported */
/** by the client */
public class NoAuthenticatorException extends ClientException {
  public NoAuthenticatorException(Exception e) {
    super(
        "service is not reporting any authenticators or none of the reported ones are supported by the client",
        e);
  }
}
