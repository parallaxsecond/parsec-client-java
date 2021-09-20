package com.github.parallaxsecond.core;

import com.github.parallaxsecond.core.ipc_handler.IpcHandler;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BasicClientTest {

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
  @Disabled
  void ping() {
    BasicClient client =
        BasicClient.client(
            "testapp", IpcHandler.connectFromUrl(URI.create("unix:/tmp/parsec.sock")));
    NativeResult.PingResult res = client.ping();
    assertEquals(Opcode.PING, res.getOpcode());
    assertEquals(1, res.getWireProtocolVersionMaj());
    assertEquals(0, res.getWireProtocolVersionMin());
  }
}
