package com.github.parallaxsecond.core;

import com.github.parallaxsecond.core.ipc_handler.IpcHandler;
import com.github.parallaxsecond.core.ipc_handler.UnixSocket;
import com.github.parallaxsecond.requests.request.Request;
import com.github.parallaxsecond.requests.response.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.ByteChannel;
import java.text.MessageFormat;
import java.time.Duration;

import static java.util.Optional.ofNullable;

/**
 * Low level client structure optimised for communicating with the service at a request level of
 * abstraction Usage is recommended when fine control over the request header and IPC handler is
 * needed.
 */
@Builder
@AllArgsConstructor
public class RequestClient {
  // FIXME taken from the rust code, seems unreasonably big
  private static final long DEFAULT_MAX_BODY_SIZE = Long.MAX_VALUE;
  /** Max size for response bodies Defaults to the max value of `usize` on the current platform */
  private long maxBodySize;
  /** Handler for IPC-related functionality Defaults to using Unix domain sockets */
  private IpcHandler ipcHandler;

  public RequestClient() {
    this(
        DEFAULT_MAX_BODY_SIZE,
        IpcHandler.connectFromUrl(
            URI.create(
                ofNullable(System.getenv("PARSEC_SERVICE_ENDPOINT"))
                    .orElse(MessageFormat.format("unix:{0}", UnixSocket.DEFAULT_SOCKET_PATH)))));
  }

  /** Send a request and get a response. */
  public Response processRequest(Request request) throws IOException {
    // Try to connect once, wait for a timeout until trying again.
    ByteChannel stream = ipcHandler.connect();
    request.writeToStream(stream);
    return Response.readFromStream(stream, maxBodySize);
  }

  public static RequestClient withDefaults() {
    return RequestClient.builder()
        .maxBodySize(DEFAULT_MAX_BODY_SIZE)
        .ipcHandler(UnixSocket.withDefaults())
        .build();
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
