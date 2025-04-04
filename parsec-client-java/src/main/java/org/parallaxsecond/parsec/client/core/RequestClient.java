package org.parallaxsecond.parsec.client.core;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.time.Duration;

import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import org.parallaxsecond.parsec.client.core.ipc_handler.UnixSocket;
import org.parallaxsecond.parsec.protocol.requests.request.Request;
import org.parallaxsecond.parsec.protocol.requests.response.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Low level client structure optimised for communicating with the service at a request level of
 * abstraction Usage is recommended when fine control over the request header and IPC handler is
 * needed.
 */
@Builder
@AllArgsConstructor
@Slf4j
public class RequestClient {
  // FIXME taken from the rust code, seems unreasonably big
  private static final long DEFAULT_MAX_BODY_SIZE = Long.MAX_VALUE;
  /**
   * Max size for response bodies Defaults to the max value of `usize` on the current platform
   */
  private long maxBodySize;
  /**
   * Handler for IPC-related functionality Defaults to using Unix domain sockets
   */
  private IpcHandler ipcHandler;

  public static RequestClient withDefaults() {
    return RequestClient.builder().maxBodySize(DEFAULT_MAX_BODY_SIZE)
        .ipcHandler(UnixSocket.withDefaults()).build();
  }

  /** Send a request and get a response. */
  public Response processRequest(Request request) throws IOException {
    log.info("Processing request: " + request);
    // Try to connect once, wait for a timeout until trying again.
    try (ByteChannel stream = ipcHandler.connect()) {
      log.info("Connected to stream");
      request.writeToStream(stream);
      log.info("Wrote request to stream");
      return Response.readFromStream(stream, maxBodySize);
    }
  }

  void setMaxBodySize(long maxBodySize) {
    this.maxBodySize = maxBodySize;
  }

  void setIpcHandler(IpcHandler ipcHandler) {
    this.ipcHandler = ipcHandler;
  }

  void setTimeout(Duration timeout) {
    this.ipcHandler.setTimeout(timeout);
  }
}
