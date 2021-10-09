package org.parallaxsecond.parsec.jca;

import org.junit.jupiter.api.Test;
import org.parallaxsecond.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

/**
 *
 */
@Testcontainers
class SecureRandomParsecTest {

  @Container
  ParsecContainer parsecContainer =
      ParsecContainer.withVersion("0.8.1")
          .withFileSystemBind(
              new File("src/test/resources/mbed-crypto-config.toml").getAbsolutePath(),
              "/etc/parsec/config.toml");

  SecureRandomParsec secureRandomParsec;

  @Test
  void engineSetSeed() {
    try {
        byte[] seed = new byte[512];
        secureRandomParsec.engineSetSeed(seed);
    }
    catch (UnsupportedOperationException e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    }
  }

  @Test
  void engineNextBytes() {
  }

  @Test
  void engineGenerateSeed() {
  }
}