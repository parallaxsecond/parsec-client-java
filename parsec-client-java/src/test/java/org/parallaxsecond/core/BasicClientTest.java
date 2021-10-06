package org.parallaxsecond.core;

import org.parallaxsecond.ParsecContainer;
import org.parallaxsecond.core.ipc_handler.IpcHandler;
import org.parallaxsecond.jna.Uid;
import org.parallaxsecond.operations.NativeResult;
import org.parallaxsecond.requests.Opcode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import psa_algorithm.PsaAlgorithm;

import java.io.File;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class BasicClientTest {

  @Container
  ParsecContainer parsecContainer =
      ParsecContainer.withVersion("0.8.1")
          .withFileSystemBind(
              new File("src/test/resources/mbed-crypto-config.toml").getAbsolutePath(),
              "/etc/parsec/config.toml");

  private BasicClient client;
  private final String eccKey = "eccKey";
  private final String rsaKey = "rsaKey";

  @BeforeEach
  void setup() {
    // uid of the parse user in docker
    Uid.IMPL.set(() -> 4000);
    Awaitility.await().until(parsecContainer::isRunning);
    this.client =
        BasicClient.client(
            "parsec-tool", IpcHandler.connectFromUrl(parsecContainer.getSocketUri()));
    parsecContainer.parsecTool("create-ecc-key", "--key-name", eccKey);
    parsecContainer.parsecTool("create-rsa-key", "--key-name", rsaKey);
  }
  /**
   * would be good to have this dockerized ssh can forward AF_UNIX sockets
   *
   * <pre>
   *  if on a mac forward the parsec socket to your local machine.
   *
   *  ssh -L/tmp/parsec.sock:/remote/home/parsec.sock 192.168.0.22
   *
   * </pre>
   */
  @Test
  void ping() {
    NativeResult.PingResult res = client.ping();
    assertEquals(Opcode.PING, res.getOpcode());
    assertEquals(1, res.getWireProtocolVersionMaj());
    assertEquals(0, res.getWireProtocolVersionMin());
  }

  @Test
  @SneakyThrows
  void listKeys() {
    NativeResult.ListKeysResult keys = client.listKeys();
    assertEquals(2, keys.getKeys().size());
  }

  @Test
  @SneakyThrows
  void hash() {
    PsaAlgorithm.Algorithm.AsymmetricSignature keyargs =
        PsaAlgorithm.Algorithm.AsymmetricSignature.newBuilder()
            .setEcdsa(
                PsaAlgorithm.Algorithm.AsymmetricSignature.Ecdsa.newBuilder()
                    .setHashAlg(
                        PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash.newBuilder()
                            .setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_256)
                            .build())
                    .build())
            .build();

    byte[] bytes = new byte[1024];
    new SecureRandom().nextBytes(bytes);
    NativeResult.PsaSignHashResult hashResult = client.psaSignHash(eccKey, bytes, keyargs);
    byte[] signature = hashResult.getSignature();
    assertNotNull(signature);

    NativeResult.PsaVerifyHashResult verifiedResult =
        client.psaVerifyHash(eccKey, bytes, keyargs, signature);
    assertNotNull(verifiedResult);

    try {
      bytes[0] += 1;
      client.psaVerifyHash(eccKey, bytes, keyargs, signature);
      fail("signature must no verify");
    } catch (Exception e) {
      // OK
    }
  }

  @Test
  void generateRandom() {
    long length = 512L;

    NativeResult.PsaGenerateRandomResult randomResult = client.psaGenerateRandom(length);
    assertNotNull(randomResult);

    byte[] randomBytes = randomResult.getRandomBytes();
    assertNotNull(randomBytes);
    assertEquals((long)randomBytes.length, length);
  }

}
