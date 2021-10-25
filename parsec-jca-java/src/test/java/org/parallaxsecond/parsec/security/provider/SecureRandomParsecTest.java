package org.parallaxsecond.parsec.security.provider;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.parallaxsecond.jna.Uid;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 *
 */
@Testcontainers
@Slf4j
class SecureRandomParsecTest {

  @Container
  ParsecContainer parsecContainer =
      ParsecContainer.withVersion("0.8.1")
          .withFileSystemBind(
              new File("src/test/resources/mbed-crypto-config.toml").getAbsolutePath(),
              "/etc/parsec/config.toml");

  private final String eccKey = "eccKey";
  private final String rsaKey = "rsaKey";
  private final Provider parsec = new Parsec();
  private final String[] algorithms = {"NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking"};

  SecureRandomParsecTest() throws IOException {
  }

  @BeforeEach
  void setup() throws IOException {
    // uid of the parse user in docker
    Uid.IMPL.set(() -> 4000);
    Awaitility.await().until(parsecContainer::isRunning);
    URI socketUri = parsecContainer.getSocketUri();
    Parsec.init(socketUri);
    parsecContainer.parsecTool("create-ecc-key", "--key-name", eccKey);
    parsecContainer.parsecTool("create-rsa-key", "--key-name", rsaKey);
    Security.insertProviderAt(parsec, 1);
    Security.getProvider(parsec.getName());
  }


  private SecureRandom getInstance(String algorithm, String provider) {
    SecureRandom secureRandomParsec = null;
    try {
      secureRandomParsec = SecureRandom.getInstance(algorithm, provider);
    } catch (UnsupportedOperationException e) {
      assertEquals(e.getClass(), UnsupportedOperationException.class);
    } catch (NoSuchAlgorithmException e) {
      fail(algorithm + " algorithm not found.", e);
    } catch (NoSuchProviderException e) {
      fail("Provider " + provider + " not found.", e);
    }
    return secureRandomParsec;
  }

  private void testSetSeed(SecureRandom secureRandom, byte[] seed) {
  }

  @Test
  void setSeedBytes() {
    byte[] seed = new byte[512];
    for (String algorithm : algorithms) {
      try {
        SecureRandom secureRandom = getInstance(algorithm, parsec.getName());
        assumeTrue(secureRandom != null);
        secureRandom.setSeed(seed);
      } catch (UnsupportedOperationException e) {
        assertEquals(e.getClass(), UnsupportedOperationException.class);
      }
    }
  }

  @Test
  void nextBytes() {
    byte[] rand = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(rand, 0, originalRand, 0, rand.length);
    assertTrue(Arrays.equals(rand, originalRand));
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = getInstance(algorithm, parsec.getName());
      secureRandom.nextBytes(rand);
      assertFalse(Arrays.equals(rand, originalRand));
      log.info("Generated random number: " + ByteBuffer.wrap(rand).getInt());
    }

  }

  @Test
  void engineGenerateSeed() {
    byte[] seed = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(seed, 0, originalRand, 0, seed.length);
    assertTrue(Arrays.equals(seed, originalRand));
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = getInstance(algorithm, parsec.getName());
      seed = secureRandom.generateSeed(4);
      assertTrue(seed.length == 4);
      log.info("Generated random seed: " + ByteBuffer.wrap(seed).getInt());
    }
  }

}