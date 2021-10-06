package org.parallaxsecond.requests;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProviderIdTest {

  @Test
  public void testOrdinal() {
    for (ProviderId id : ProviderId.values()) {
      assertEquals(id.getId(), id.ordinal());
    }
  }
}
