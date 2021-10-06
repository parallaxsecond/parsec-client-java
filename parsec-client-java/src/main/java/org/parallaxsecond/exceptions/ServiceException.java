package org.parallaxsecond.exceptions;

import org.parallaxsecond.requests.ResponseStatus;

public class ServiceException extends RuntimeException {
  private final ResponseStatus responseStatus;

  public ServiceException(ResponseStatus responseStatus) {
    super(responseStatus.toString(), null);
    this.responseStatus = responseStatus;
  }
}
