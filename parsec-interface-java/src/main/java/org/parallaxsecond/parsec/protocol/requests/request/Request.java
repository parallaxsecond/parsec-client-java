package org.parallaxsecond.parsec.protocol.requests.request;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.MessageFormat;

import org.parallaxsecond.parsec.protocol.requests.InterfaceException;
import org.parallaxsecond.parsec.protocol.requests.ResponseStatus;
import org.parallaxsecond.parsec.protocol.requests.request.common.WireHeader_1_0;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

/** Representation of the request wire format. */
@Builder
@RequiredArgsConstructor
public class Request {
  /** Request header */
  private final RequestHeader header;
  /**
   * Request body consists of `RequestBody` object holding a collection of bytes. Interpretation of
   * said bytes is deferred to the a converter which can handle the `content_type` defined in the
   * header.
   */
  private final RequestBody body;

  /**
   * Auth field is stored as a `RequestAuth` object. A parser that can handle the `auth_type`
   * specified in the header is needed to authenticate the request.
   */
  private final RequestAuth auth;

  /**
   * Deserialise request from given stream.
   *
   * <p>
   * Request header is parsed from its raw form, ensuring that all fields are valid. The
   * `body_len_limit` parameter allows the interface client to reject requests that are longer than
   * a predefined limit. The length limit is in bytes.
   *
   * <p>
   * # Errors - if reading any of the subfields (header, body or auth) fails, the corresponding
   * `ResponseStatus` will be returned. - if the request body size specified in the header is larger
   * than the limit passed as a parameter, `BodySizeExceedsLimit` will be returned.
   */
  public static Request readFromStream(ReadableByteChannel channel, int bodyLenLimit)
      throws IOException {
    WireHeader_1_0 rawHeader = WireHeader_1_0.readFromStream(channel);
    int bodyLen = rawHeader.getBodyLen();
    if (bodyLen > bodyLenLimit) {
      throw new InterfaceException(ResponseStatus.BodySizeExceedsLimit, MessageFormat.format(
          "Request body length ({0}) bigger than the limit given ({1}).", bodyLen, bodyLenLimit));
    }
    RequestBody body = RequestBody.readFromStream(channel, bodyLen);
    RequestAuth auth = RequestAuth.readFromStream(channel, rawHeader.getAuthLen());

    return Request.builder().header(RequestHeader.fromRaw(rawHeader)).body(body).auth(auth).build();
  }

  /**
   * Serialise request and write it to given stream.
   *
   * <p>
   * Request header is first converted to its raw format before serialization. # Errors - if an IO
   * operation fails while writing any of the subfields of the request,
   * `ResponseStatus::ConnectionError` is returned. - if encoding any of the fields in the header
   * fails, `ResponseStatus::InvalidEncoding` is returned.
   */
  public void writeToStream(WritableByteChannel channel) throws IOException {
    header.toRaw().bodyLen(body.length()).authLen((short) auth.getBuffer().length()).build()
        .writeToStream(channel);
    body.writeToStream(channel);
    auth.writeToStream(channel);
  }

  @Override
  public String toString() {
    return "Request{" + "header=" + header + ", body=" + body + ", auth=" + auth + '}';
  }

}
