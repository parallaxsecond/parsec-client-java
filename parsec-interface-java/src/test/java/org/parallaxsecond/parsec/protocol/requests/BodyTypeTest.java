package org.parallaxsecond.parsec.protocol.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BodyTypeTest {

  @Test
  void testOrdinal() {
    for (BodyType bodyType : BodyType.values()) {
      assertEquals(bodyType.getId(), bodyType.ordinal());
    }
  }
}
