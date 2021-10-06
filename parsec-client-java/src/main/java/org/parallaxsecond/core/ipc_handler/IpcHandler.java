package org.parallaxsecond.core.ipc_handler;

import org.parallaxsecond.exceptions.InvalidSocketUrlException;
import lombok.NonNull;

import java.net.URI;
import java.nio.channels.ByteChannel;
import java.time.Duration;

public interface IpcHandler {
  /// Default timeout for client IPC requests.
  Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

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
