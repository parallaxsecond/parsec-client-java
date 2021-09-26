package com.github.parallaxsecond.operations;

import com.github.parallaxsecond.requests.BodyType;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;

/**
 * Definition of the operations converters must implement to allow usage of a specific `BodyType`.
 */
public interface Convert {
  /** Get the `BodyType` associated with this converter. */
  BodyType bodyType();

  /**
   * Create a native operation object from a request body.
   *
   * <p># Errors - if deserialization fails, `ResponseStatus::DeserializingBodyFailed` is returned
   */
  NativeOperation bodyToOperation(RequestBody body, Opcode opcode) throws Exception;

  /**
   * Create a request body from a native operation object. # Errors - if serialization fails,
   * `ResponseStatus::SerializingBodyFailed` is returned
   */
  RequestBody operationToBody(NativeOperation operation);

  /**
   * Create a native result object from a response body. # Errors - if deserialization fails,
   * `ResponseStatus::DeserializingBodyFailed` is returned
   */
  NativeResult bodyToResult(ResponseBody body, Opcode opcode);

  /**
   * Create a response body from a native result object. # Errors - if serialization fails,
   * `ResponseStatus::SerializingBodyFailed` is returned
   */
  ResponseBody resultToBody(NativeResult result);
}
