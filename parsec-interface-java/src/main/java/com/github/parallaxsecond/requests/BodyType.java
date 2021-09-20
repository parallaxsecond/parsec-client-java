package com.github.parallaxsecond.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Listing of body encoding types and their associated codes.
 *
 * <p>Passed in headers as `content_type` and `accept_type`.
 */
@RequiredArgsConstructor
@Getter
public enum BodyType {
  /** Protobuf format for operations. */
  PROTOBUF((byte) 0);
  private final byte id;

  public static BodyType fromCode(byte code) {
    return values()[code];
  }
}
