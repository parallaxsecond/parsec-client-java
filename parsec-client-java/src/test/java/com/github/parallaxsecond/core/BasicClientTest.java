package com.github.parallaxsecond.core;

import com.github.parallaxsecond.ParsecContainer;
import com.github.parallaxsecond.core.ipc_handler.IpcHandler;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import psa_algorithm.PsaAlgorithm;

import java.net.URI;
import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class BasicClientTest {

  @Container
  ParsecContainer parsecContainer = ParsecContainer.withVersion("0.8.1");

  private BasicClient client;

  @BeforeEach
  void setup() {
    Awaitility.await().until(parsecContainer::isRunning);
    this.client =
            BasicClient.client(
                    "testapp", IpcHandler.connectFromUrl(parsecContainer.getSocketUri()));
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
  void hash() {
    PsaAlgorithm.Algorithm.AsymmetricSignature keyargs =
        PsaAlgorithm.Algorithm.AsymmetricSignature.newBuilder()
            .setEcdsa(
                PsaAlgorithm.Algorithm.AsymmetricSignature.Ecdsa.newBuilder()
                    .setHashAlg(
                        PsaAlgorithm.Algorithm.AsymmetricSignature.SignHash.newBuilder()
                            .setSpecific(PsaAlgorithm.Algorithm.Hash.SHA_512)
                            .build())
                    .build())
            .build();
    byte[] bytes = new byte[1024];
    new SecureRandom().nextBytes(bytes);
    NativeResult.PsaSignHashResult res = client.psaSignHash("some.key", bytes, keyargs);
    byte[] signature = res.getSignature();
    assertNotNull(signature);

  }
}
