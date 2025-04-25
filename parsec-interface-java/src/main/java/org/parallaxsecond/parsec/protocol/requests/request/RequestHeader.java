package org.parallaxsecond.parsec.protocol.requests.request;

import org.parallaxsecond.parsec.protocol.requests.AuthType;
import org.parallaxsecond.parsec.protocol.requests.BodyType;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.ProviderId;
import org.parallaxsecond.parsec.protocol.requests.request.common.WireHeader_1_0;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A native representation of the request header.
 *
 * <p>
 * Fields that are not relevant for application development (e.g. magic number) are not copied
 * across from the raw header.
 */
@RequiredArgsConstructor
@Builder
@Getter
public class RequestHeader {
  /** Provider ID value */
  private final ProviderId provider;
  /** Session handle */
  private final long session;
  /** Content type: defines how the request body should be processed. */
  private final BodyType contentType;
  /** Accept type: defines how the service should provide its response. */
  private final BodyType acceptType;
  /** Authentication type. */
  private final AuthType authType;
  /** Opcode of the operation to perform. */
  private final Opcode opcode;

  public static RequestHeader fromRaw(WireHeader_1_0 wireHeader) {
    return RequestHeader.builder().provider(ProviderId.fromCode(wireHeader.getProvider()))
        .session(wireHeader.getSession())
        .contentType(BodyType.fromCode(wireHeader.getContentType()))
        .acceptType(BodyType.fromCode(wireHeader.getAcceptType()))
        .authType(AuthType.fromCode(wireHeader.getAuthType()))
        .opcode(Opcode.fromCode(wireHeader.getOpcode())).build();
  }

  public WireHeader_1_0.WireHeader_1_0Builder toRaw() {
    return WireHeader_1_0.builder().flags((short) 0).provider(getProvider().getId())
        .session(getSession()).contentType(getContentType().getId())
        .acceptType(getAcceptType().getId()).authType(getAuthType().getId()).bodyLen(0)
        .authLen((short) 0).opcode(getOpcode().getCode()).status((short) 0).reserved1((byte) 0)
        .reserved2((byte) 0);
  }

  @Override
  public String toString() {
    return "RequestHeader{" + "provider=" + provider + ", session=" + session + ", contentType="
        + contentType + ", acceptType=" + acceptType + ", authType=" + authType + ", opcode="
        + opcode + '}';
  }
}
