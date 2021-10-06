package org.parallaxsecond.requests.request.common;

import org.parallaxsecond.exceptions.InterfaceException;
import org.parallaxsecond.requests.ResponseStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.MessageFormat;

/**
 * This module defines and implements the raw wire protocol header frame for version 1.0 of the
 * protocol.
 *
 * <p>Raw representation of a common request/response header, as defined for the wire format.
 *
 * <p>Serialisation and deserialisation are handled by `serde`, also in tune with the wire format
 * (i.e. little-endian, native encoding).
 */
@RequiredArgsConstructor
@Builder
@Getter
public class WireHeader_1_0 {
  public static final int MAGIC_NUMBER = 0x5EC0_A710;
  public static final byte WIRE_PROTOCOL_VERSION_MAJ = 1;
  public static final byte WIRE_PROTOCOL_VERSION_MIN = 0;
  public static final short REQUEST_HDR_SIZE = 30;

  /**
   * Implementation-defined flags. Not used in Parsec currently. Must be present, but must be zero.
   */
  private final short flags;
  /** Provider ID value */
  private final byte provider;
  /** Session handle */
  private final long session;
  /** Content type: defines how the request body should be processed. */
  private final byte contentType;
  /** Accept type: defines how the service should provide its response. */
  private final byte acceptType;
  /** Authentication type. */
  private final byte authType;
  /** Number of bytes of content. */
  private final int bodyLen;
  /** Number of bytes of authentication. */
  private final short authLen;
  /** Opcode of the operation to perform. */
  private final int opcode;
  /** Response status of the request. */
  private final short status;
  /** Reserved byte. Currently unused. Must be present. Must be zero. */
  private final byte reserved1;
  /** Reserved byte. Currently unused. Must be present. Must be zero. */
  private final byte reserved2;

  public static WireHeader_1_0 readFromStream(ReadableByteChannel channel) throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(REQUEST_HDR_SIZE + 6).order(ByteOrder.LITTLE_ENDIAN);

    channel.read(buf);
    buf.flip();

    int magicNumber = buf.getInt();
    if (magicNumber != MAGIC_NUMBER) {
      throw new InterfaceException(
          ResponseStatus.InvalidHeader,
          MessageFormat.format("Expected magic number {0}, got {1}", MAGIC_NUMBER, magicNumber));
    }
    short hdrSize = buf.getShort();
    if (hdrSize != REQUEST_HDR_SIZE || buf.remaining() < hdrSize) {
      throw new InterfaceException(
          ResponseStatus.InvalidHeader,
          MessageFormat.format(
              "Expected request header size {0}, got {1}, remaining {2}",
              REQUEST_HDR_SIZE, hdrSize, buf.remaining()));
    }
    int versionMaj = buf.get();
    int versionMin = buf.get();
    if (versionMaj != WIRE_PROTOCOL_VERSION_MAJ || versionMin != WIRE_PROTOCOL_VERSION_MIN) {
      throw new InterfaceException(
          ResponseStatus.WireProtocolVersionNotSupported,
          MessageFormat.format(
              "Expected wire protocol version {0}.{1}, got {2}.{3} instead",
              WIRE_PROTOCOL_VERSION_MAJ, WIRE_PROTOCOL_VERSION_MIN, versionMaj, versionMin));
    }

    WireHeader_1_0 wireHeader =
        WireHeader_1_0.builder()
            .flags(buf.getShort())
            .provider(buf.get())
            .session(buf.getLong())
            .contentType(buf.get())
            .acceptType(buf.get())
            .authType(buf.get())
            .bodyLen(buf.getInt())
            .authLen(buf.getShort())
            .opcode(buf.getInt())
            .status(buf.getShort())
            .reserved1(buf.get())
            .reserved2(buf.get())
            .build();

    if (wireHeader.reserved1 != 0x00 || wireHeader.reserved2 != 0x00) {
      throw new InterfaceException(
          ResponseStatus.InvalidHeader,
          MessageFormat.format(
              "expected reserved1 0, got {0}, reserved2 0, got {1}",
              wireHeader.reserved1, wireHeader.reserved2));
    }
    return wireHeader;
  }

  /**
   * Serialise the request header and write the corresponding bytes to the given stream.
   *
   * <p># Errors - if marshalling the header fails, `ResponseStatus::InvalidEncoding` is returned. -
   * if writing the header bytes fails, `ResponseStatus::ConnectionError` is returned.
   */
  public void writeToStream(WritableByteChannel channel) throws IOException {
    ByteBuffer buf =
        ByteBuffer.allocate(REQUEST_HDR_SIZE + 6)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(MAGIC_NUMBER) // 4
            .putShort(REQUEST_HDR_SIZE) // 6
            .put(WIRE_PROTOCOL_VERSION_MAJ) // 7
            .put(WIRE_PROTOCOL_VERSION_MIN) // 8
            .putShort(flags) // 10
            .put(provider) // 11
            .putLong(session) // 19
            .put(contentType) // 20
            .put(acceptType) // 21
            .put(authType) // 22
            .putInt(bodyLen) // 26
            .putShort(authLen) // 28
            .putInt(opcode) // 32
            .putShort(status) // 34
            .put(reserved1) // 35
            .put(reserved2); // 36
    buf.flip();

    channel.write(buf);
  }
}
