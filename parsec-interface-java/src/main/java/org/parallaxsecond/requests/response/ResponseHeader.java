package org.parallaxsecond.requests.response;

import org.parallaxsecond.requests.BodyType;
import org.parallaxsecond.requests.Opcode;
import org.parallaxsecond.requests.ProviderId;
import org.parallaxsecond.requests.ResponseStatus;
import org.parallaxsecond.requests.request.common.WireHeader_1_0;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A native representation of the response header.
 *
 * <p>Fields that are not relevant for application development (e.g. magic number) are not copied
 * across from the raw header.
 */
@RequiredArgsConstructor
@Builder
@Getter
public class ResponseHeader {
  /** Provider ID value */
  private final ProviderId provider;
  /** Session handle */
  private final long session;
  /** Content type: defines how the request body should be processed. */
  private final BodyType contentType;
  /** Opcode of the operation to perform. */
  private final Opcode opcode;
  /** Response status of the request. */
  private final ResponseStatus status;

  public static ResponseHeader fromRaw(WireHeader_1_0 wireHeader) {
    return ResponseHeader.builder()
        .provider(ProviderId.fromCode(wireHeader.getProvider()))
        .session(wireHeader.getSession())
        .contentType(BodyType.fromCode(wireHeader.getContentType()))
        .opcode(Opcode.fromCode(wireHeader.getOpcode()))
        .status(ResponseStatus.fromCode(wireHeader.getStatus()))
        .build();
  }

  public WireHeader_1_0.WireHeader_1_0Builder toRaw() {
    return WireHeader_1_0.builder()
        .flags((short) 0)
        .provider(provider.getId())
        .session(session)
        .contentType(contentType.getId())
        .acceptType((byte) 0)
        .authType((byte) 0)
        .authLen((short) 0)
        .bodyLen(0)
        .opcode(opcode.getCode())
        .status(status.getId())
        .reserved1((byte) 0)
        .reserved2((byte) 0);
  }
}
