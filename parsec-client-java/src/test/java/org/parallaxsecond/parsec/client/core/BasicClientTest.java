package org.parallaxsecond.parsec.client.core;

import java.security.SecureRandom;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;
import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.testcontainers.ParsecContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Testcontainers
@Slf4j
class BasicClientTest {

  @Container
  ParsecContainer parsecContainer = ParsecContainer.withVersion("latest");
  // .withCopyToContainer(CONFIG_FILE, "/etc/parsec/config.toml");

  private BasicClient client;
  private final String eccKey = "eccKey";
  private final String rsaKey = "rsaKey";
  PsaKeyAttributes.KeyAttributes eccKeyAttributes =
      PsaKeyAttributes.KeyAttributes.newBuilder().setKeyBits(256)
          .setKeyType(PsaKeyAttributes.KeyType.newBuilder()
              .setEccKeyPair(PsaKeyAttributes.KeyType.EccKeyPair.newBuilder()
                  .setCurveFamily(PsaKeyAttributes.KeyType.EccFamily.SECP_R1).build())
              .build())
          .setKeyPolicy(
              PsaKeyAttributes.KeyPolicy.newBuilder()
                  .setKeyUsageFlags(
                      PsaKeyAttributes.UsageFlags.newBuilder().setSignHash(true).setVerifyHash(true)
                          .setSignMessage(true).setVerifyMessage(true).setExport(true).build())
                  .setKeyAlgorithm(
                      PsaAlgorithm.Algorithm.newBuilder()
                          .setAsymmetricSignature(PsaAlgorithm.Algorithm.AsymmetricSignature
                              .newBuilder()
                              .setEcdsa(PsaAlgorithm.Algorithm.AsymmetricSignature.Ecdsa
                                  .newBuilder()
                                  .setHashAlg(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash
                                      .newBuilder().setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_256)
                                      .build())
                                  .build())
                              .build()))
                  .build())
          .build();


  PsaAlgorithm.Algorithm.AsymmetricSignature eccKeyArgs =
      PsaAlgorithm.Algorithm.AsymmetricSignature.newBuilder()
          .setEcdsa(
              PsaAlgorithm.Algorithm.AsymmetricSignature.Ecdsa
                  .newBuilder().setHashAlg(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash
                      .newBuilder().setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_256).build())
                  .build())
          .build();


  PsaKeyAttributes.KeyAttributes rsaKeyAttributes = PsaKeyAttributes.KeyAttributes.newBuilder()
      .setKeyBits(1024)
      .setKeyType(PsaKeyAttributes.KeyType.newBuilder()
          .setRsaKeyPair(PsaKeyAttributes.KeyType.RsaKeyPair.newBuilder().build()).build())
      .setKeyPolicy(PsaKeyAttributes.KeyPolicy.newBuilder()
          .setKeyUsageFlags(
              PsaKeyAttributes.UsageFlags.newBuilder().setSignHash(true).setVerifyHash(true)
                  .setSignMessage(true).setVerifyMessage(true).setExport(true).build())
          .setKeyAlgorithm(PsaAlgorithm.Algorithm.newBuilder()
              .setAsymmetricSignature(PsaAlgorithm.Algorithm.AsymmetricSignature.newBuilder()
                  .setRsaPkcs1V15Sign(PsaAlgorithm.Algorithm.AsymmetricSignature.RsaPkcs1v15Sign
                      .newBuilder()
                      .setHashAlg(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash.newBuilder()
                          .setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_256).build())
                      .build())
                  .build()))
          .build())
      .build();


  PsaAlgorithm.Algorithm.AsymmetricSignature rsaKeyArgs =
      PsaAlgorithm.Algorithm.AsymmetricSignature.newBuilder()
          .setRsaPkcs1V15Sign(
              PsaAlgorithm.Algorithm.AsymmetricSignature.RsaPkcs1v15Sign
                  .newBuilder().setHashAlg(PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash
                      .newBuilder().setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_256).build())
                  .build())
          .build();



  @BeforeEach
  void setup() {

    log.info("Setting up test");
    // uid of the parse user in docker
    Uid.IMPL.set(() -> 4000);

    // Wait for container to be running
    Awaitility.await().until(parsecContainer::isRunning);

    // Print container logs for debugging
    log.info("Container logs:");
    log.info(parsecContainer.getLogs());

    // Wait a bit for socat to be ready
    Awaitility.await().pollDelay(Duration.ofSeconds(1)).until(() -> true);

    this.client = BasicClient.client("parsec-tool",
        IpcHandler.connectFromUrl(parsecContainer.getSocketUri()));
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
  void hashRSAKey() {

    String keyName = "rsa-test-key";
    client.psaGenerateKey(keyName, rsaKeyAttributes);

    byte[] bytes = new byte[1024];
    new SecureRandom().nextBytes(bytes);
    try {
      NativeResult.PsaSignHashResult hashResult = client.psaSignHash(keyName, bytes, rsaKeyArgs);
      byte[] signature = hashResult.getSignature();
      assertNotNull(signature);

      NativeResult.PsaVerifyHashResult verifiedResult =
          client.psaVerifyHash(keyName, bytes, rsaKeyArgs, signature);
      assertNotNull(verifiedResult);

      try {
        bytes[0] += 1;
        client.psaVerifyHash(keyName, bytes, rsaKeyArgs, signature);
        fail("signature must no verify");
      } catch (Exception e) {
        // OK
      }
    } catch (Exception e) {
      // wait 60 seconds so I can analyze docker output, then end
      Thread.sleep(30000L);
    }
  }

  @Test
  @SneakyThrows
  void hashECCKey() {

    String keyName = "ecc-test-key";
    client.psaGenerateKey(keyName, eccKeyAttributes);

    byte[] bytes = new byte[1024];
    new SecureRandom().nextBytes(bytes);
    try {
      NativeResult.PsaSignHashResult hashResult = client.psaSignHash(keyName, bytes, eccKeyArgs);
      byte[] signature = hashResult.getSignature();
      assertNotNull(signature);

      NativeResult.PsaVerifyHashResult verifiedResult =
          client.psaVerifyHash(keyName, bytes, eccKeyArgs, signature);
      assertNotNull(verifiedResult);

      try {
        bytes[0] += 1;
        client.psaVerifyHash(keyName, bytes, rsaKeyArgs, signature);
        fail("signature must no verify");
      } catch (Exception e) {
        // OK
      }
    } catch (Exception e) {
      // wait 60 seconds so I can analyze docker output, then end
      Thread.sleep(30000L);
    }
  }

  @Test
  void generateRandom() {
    long length = 512L;

    byte[] randomBytes = client.psaGenerateRandom(length);

    assertNotNull(randomBytes);
    assertEquals((long) randomBytes.length, length);
  }
}
