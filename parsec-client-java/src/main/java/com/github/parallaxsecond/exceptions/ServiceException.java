package com.github.parallaxsecond.exceptions;

import com.github.parallaxsecond.requests.ResponseStatus;

public class ServiceException extends RuntimeException {
  private final ResponseStatus responseStatus;

  public ServiceException(ResponseStatus responseStatus) {
    super(responseStatus.getDescription(), null);
    this.responseStatus = responseStatus;
  }
}
