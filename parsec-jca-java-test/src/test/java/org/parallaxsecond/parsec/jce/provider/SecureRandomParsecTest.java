package org.parallaxsecond.parsec.jce.provider;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

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

  Provider parsec;
  private final String[] algorithms = {"NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking"};


  @BeforeEach
  void setup() {
    // uid of the parse user in docker
    Uid.IMPL.set(() -> 4000);
    Awaitility.await().until(parsecContainer::isRunning);
    URI socketUri = parsecContainer.getSocketUri();
    parsec = ParsecProvider.builder()
            .socketUri(socketUri)
            .build();
    String eccKey = "eccKey";
    parsecContainer.parsecTool("create-ecc-key", "--key-name", eccKey);
    String rsaKey = "rsaKey";
    parsecContainer.parsecTool("create-rsa-key", "--key-name", rsaKey);
    Security.insertProviderAt(parsec, 1);
    Security.getProvider(parsec.getName());
  }

  @Test
  @SneakyThrows
  void setSeedBytes() {
    byte[] seed = new byte[512];
    for (String algorithm : algorithms) {
      try {
        SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
        assertNotNull(secureRandom);
        secureRandom.setSeed(seed);
      } catch (UnsupportedOperationException e) {
        assertEquals(e.getClass(), UnsupportedOperationException.class);
      }
    }
  }

  @Test
  @SneakyThrows
  void nextBytes() {
    byte[] rand = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(rand, 0, originalRand, 0, rand.length);
    assertArrayEquals(rand, originalRand);
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
      secureRandom.nextBytes(rand);
      assertFalse(Arrays.equals(rand, originalRand));
      log.info("Generated random number: " + ByteBuffer.wrap(rand).getInt());
    }

  }

  @Test
  @SneakyThrows
  void engineGenerateSeed() {
    byte[] seed = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(seed, 0, originalRand, 0, seed.length);
    assertArrayEquals(seed, originalRand);
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
      seed = secureRandom.generateSeed(4);
      assertEquals(4, seed.length);
      log.info("Generated random seed: " + ByteBuffer.wrap(seed).getInt());
    }
  }

}