package org.parallaxsecond.parsec.protocol.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Listing of available authentication methods.
 *
 * <p>Passed in headers as `auth_type`.
 */
@RequiredArgsConstructor
@Getter
public enum AuthType {
  /** No authentication */
  NO_AUTH((byte) 0, "No authentication"),
  /** Direct authentication */
  DIRECT((byte) 1, "Direct authentication"),
  /** JSON Web Tokens (JWT) authentication (not currently supported) */
  JWT((byte) 2, "JSON Web Tokens authentication"),
  /** Unix peer credentials authentication */
  UNIX_PEER_CREDENTIALS((byte) 3, "Unix Peer Credentials authentication"),
  /** Authentication verifying a JWT SPIFFE Verifiable Identity Document */
  JWT_SVID((byte) 4, "JWT SPIFFE Verifiable Identity Document authentication");

  private final byte id;
  private final String description;

  public static AuthType fromCode(byte authType) {
    return values()[authType];
  }

  @Override
  public String toString() {
    return description;
  }
}
