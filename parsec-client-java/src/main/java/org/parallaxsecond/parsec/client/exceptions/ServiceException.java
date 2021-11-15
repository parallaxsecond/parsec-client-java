package org.parallaxsecond.parsec.client.exceptions;

import org.parallaxsecond.parsec.protocol.requests.ResponseStatus;

public class ServiceException extends RuntimeException {
  private final ResponseStatus responseStatus;

  public ServiceException(ResponseStatus responseStatus) {
    super(responseStatus.toString(), null);
    this.responseStatus = responseStatus;
  }
}
