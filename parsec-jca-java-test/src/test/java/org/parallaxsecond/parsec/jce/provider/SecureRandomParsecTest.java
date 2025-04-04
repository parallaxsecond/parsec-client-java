package org.parallaxsecond.parsec.jce.provider;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class SecureRandomParsecTest {

  @Container
  ParsecContainer parsecContainer = ParsecContainer.withVersion("latest");

  Provider parsec;
  private final String[] algorithms = {"NativePRNG", "NativePRNGBlocking", "NativePRNGNonBlocking"};


  @BeforeEach
  void setup() {
    // uid of the parse user in docker
    Uid.IMPL.set(() -> 4000);
    Awaitility.await().until(parsecContainer::isRunning);
    URI socketUri = parsecContainer.getSocketUri();
    parsec = ParsecProvider.builder().socketUri(socketUri).build();


    String eccKey = "eccKey";
    parsecContainer.parsecTool("create-ecc-key", "--key-name", eccKey);
    String rsaKey = "rsaKey";
    parsecContainer.parsecTool("create-rsa-key", "--key-name", rsaKey);
    Security.insertProviderAt(parsec, 1);
    Security.getProvider(parsec.getName());
  }

  @AfterEach
  void cleanup() {
    if (parsec != null) {
      Security.removeProvider(parsec.getName());
    }
    // Force close any lingering connections
    if (parsecContainer != null) {
      parsecContainer.close();
    }
  }

  @Test
  @SneakyThrows
  @Order(1)
  void setSeedBytes() {
    byte[] seed = new byte[512];
    for (String algorithm : algorithms) {
      try {
        SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
        assertNotNull(secureRandom);
        secureRandom.setSeed(seed);
      } catch (UnsupportedOperationException e) {
        assertEquals(e.getClass(), UnsupportedOperationException.class);
      } catch (NoSuchAlgorithmException e) {
        fail("No such algorithm: " + algorithm);
      }
    }
  }

  @Test
  @SneakyThrows
  @Order(1)
  void nextBytes() {
    byte[] rand = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(rand, 0, originalRand, 0, rand.length);
    assertArrayEquals(rand, originalRand);
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
      secureRandom.nextBytes(rand);
      assertFalse(Arrays.equals(rand, originalRand));
    }
  }



  @Test
  @SneakyThrows
  @Order(2)
  void engineGenerateSeed() {
    byte[] seed = new byte[4];
    byte[] originalRand = new byte[4];
    System.arraycopy(seed, 0, originalRand, 0, seed.length);
    assertArrayEquals(seed, originalRand);
    for (String algorithm : algorithms) {
      SecureRandom secureRandom = SecureRandom.getInstance(algorithm, parsec.getName());
      seed = secureRandom.generateSeed(4);
      assertEquals(4, seed.length);
    }
  }

}
