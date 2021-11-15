package org.parallaxsecond.parsec.client;

import org.parallaxsecond.parsec.client.exceptions.SpiffeException;
import org.parallaxsecond.parsec.client.jna.Uid;
import org.parallaxsecond.parsec.protocol.requests.AuthType;
import org.parallaxsecond.parsec.protocol.requests.request.RequestAuth;
import io.spiffe.exception.JwtSvidException;
import io.spiffe.exception.SocketEndpointAddressException;
import io.spiffe.workloadapi.DefaultWorkloadApiClient;
import io.spiffe.workloadapi.WorkloadApiClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static java.nio.ByteOrder.LITTLE_ENDIAN;

/** Authentication data used in Parsec requests */
public interface Authentication {
  AuthType getAuthType();

  RequestAuth createRequestAuth();

  /** Used in cases where no authentication is desired or required */
  class None implements Authentication {
    @Getter private final AuthType authType = AuthType.NO_AUTH;

    @Override
    public RequestAuth createRequestAuth() {
      return new RequestAuth();
    }
  }
  /**
   * Data used for direct, identity-based authentication
   *
   * <p>Warning: Systems using direct authentication require extra measures to be as secure as
   * deployments with other authentication mechanisms. Please check the [Parsec Threat
   * Model](https://parallaxsecond.github.io/parsec-book/parsec_security/parsec_threat_model/threat_model.html)
   * for more information.
   */
  @RequiredArgsConstructor
  class Direct implements Authentication {
    @Getter private final AuthType authType = AuthType.DIRECT;
    @NonNull private final String name;

    @Override
    public RequestAuth createRequestAuth() {
      return new RequestAuth(name.getBytes(StandardCharsets.UTF_8));
    }
  }
  /**
   * Used for authentication via Peer Credentials provided by Unix operating systems for Domain
   * Socket connections.
   */
  class UnixPeerCredentials implements Authentication {
    @Getter private final AuthType authType = AuthType.UNIX_PEER_CREDENTIALS;

    @Override
    public RequestAuth createRequestAuth() {
      int currentUId = Uid.getUid();
      return new RequestAuth(
          ByteBuffer.allocate(4).order(LITTLE_ENDIAN).putInt(currentUId).array());
    }
  }
  /**
   * Authentication using JWT SVID tokens. The will fetch its JWT-SVID and pass it in the
   * Authentication field. The socket endpoint is found through the SPIFFE_ENDPOINT_SOCKET
   * environment variable.
   */
  class JwtSvid implements Authentication {
    @Getter private final AuthType authType = AuthType.JWT_SVID;

    @Override
    public RequestAuth createRequestAuth() {
      try (WorkloadApiClient client = DefaultWorkloadApiClient.newClient()) {
        io.spiffe.svid.jwtsvid.JwtSvid token = client.fetchJwtSvid("parsec");
        return new RequestAuth(token.getToken().getBytes(StandardCharsets.UTF_8));
      } catch (IOException | SocketEndpointAddressException | JwtSvidException e) {
        throw new SpiffeException(e);
      }
    }
  }
}
