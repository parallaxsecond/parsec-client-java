package com.github.parallaxsecond.exceptions;

/** Error while using the SPIFFE Workload API */
public class SpiffeException extends ClientException {
  public SpiffeException(Exception e) {
    super(e.getMessage(), e);
  }
}
