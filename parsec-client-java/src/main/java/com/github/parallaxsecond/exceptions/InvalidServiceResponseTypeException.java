package com.github.parallaxsecond.exceptions;

import com.github.parallaxsecond.requests.Opcode;

/** The opcode of the response does not match the opcode of the request */
public class InvalidServiceResponseTypeException extends ClientException {
  public InvalidServiceResponseTypeException() {
    super("", null);
  }

  public InvalidServiceResponseTypeException(Opcode expected, Opcode actual) {
    super(
        "the opcode of the response does not match the opcode of the request."
            + "Expected: "
            + expected
            + ", Actual: "
            + actual,
        null);
  }
}
