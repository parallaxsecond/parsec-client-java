package com.github.parallaxsecond.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthTypeTest {

  @Test
  void testOrdinal() {
    for (AuthType authType : AuthType.values()) {
      assertEquals(authType.getId(), authType.ordinal());
    }
  }
}
