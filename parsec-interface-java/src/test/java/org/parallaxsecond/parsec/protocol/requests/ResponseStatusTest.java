package org.parallaxsecond.parsec.protocol.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResponseStatusTest {

  @Test
  void testFromCode() {
    for (ResponseStatus responseStatus : ResponseStatus.values()) {
      assertEquals(responseStatus, ResponseStatus.fromCode(responseStatus.getId()));
    }
  }
}
