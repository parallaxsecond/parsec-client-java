package org.parallaxsecond.parsec.client.core.ipc_handler;

import java.net.URI;
import java.nio.channels.ByteChannel;
import java.time.Duration;

import org.parallaxsecond.parsec.client.exceptions.InvalidSocketUrlException;

import lombok.NonNull;

public interface IpcHandler {
  /// Default timeout for client IPC requests.
  Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

  static IpcHandler connectFromUrl(@NonNull URI uri) {
    switch (uri.getScheme()) {
      case "unix":
        return new UnixSocket(uri.getPath());
      default:
        throw new InvalidSocketUrlException(uri);
    }
  }

  ByteChannel connect();

  void setTimeout(Duration timeout);
}
