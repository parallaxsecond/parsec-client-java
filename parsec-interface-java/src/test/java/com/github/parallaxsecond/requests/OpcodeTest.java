package com.github.parallaxsecond.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpcodeTest {

  @Test
  void testFromCode() {
    for (Opcode opcode : Opcode.values()) {
      assertEquals(opcode, Opcode.fromCode(opcode.getCode()));
    }
  }

  @Test
  void isCrypto() {
    assertFalse(Opcode.LIST_KEYS.isCrypto());
    assertTrue(Opcode.PSA_GENERATE_KEY.isCrypto());
  }

  @Test
  void isCore() {
    assertTrue(Opcode.LIST_KEYS.isCore());
  }

  @Test
  void isAdmin() {
    assertTrue(Opcode.LIST_CLIENTS.isAdmin());
    assertFalse(Opcode.PSA_GENERATE_KEY.isAdmin());
  }
}
