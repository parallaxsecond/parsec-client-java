package org.parallaxsecond.requests.response;

import org.parallaxsecond.exceptions.InterfaceException;
import org.parallaxsecond.requests.ResponseStatus;
import org.parallaxsecond.requests.request.common.WireHeader_1_0;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.text.MessageFormat;

/** Native representation of the response wire format. */
@RequiredArgsConstructor
@Builder
@Getter
public class Response {
  /** Header of the response, containing the response status. */
  private final ResponseHeader header;
  /**
   * Response body consists of an opaque vector of bytes. Interpretation of said bytes is deferred
   * to the a converter which can handle the `content_type` defined in the header.
   */
  private final ResponseBody body;

  /**
   * Deserialise response from given stream.
   *
   * <p>The `bodyLenLimit` parameter allows the interface client to reject requests that are longer
   * than a predefined limit. The length limit is in bytes. /** # Errors - if reading any of the
   * subfields (header or body) fails, the corresponding `ResponseStatus` will be returned. - if the
   * request body size specified in the header is larger than the limit passed as a parameter,
   * `BodySizeExceedsLimit` will be returned.
   */
  public static Response readFromStream(ReadableByteChannel channel, long bodyLenLimit)
      throws IOException {

    WireHeader_1_0 rawHeader = WireHeader_1_0.readFromStream(channel);
    int bodyLen = rawHeader.getBodyLen();
    if (bodyLen > bodyLenLimit) {
      throw new InterfaceException(
          ResponseStatus.BodySizeExceedsLimit,
          MessageFormat.format(
              "Request body length ({0}) bigger than the limit given ({1}).",
              bodyLen, bodyLenLimit));
    }

    ResponseBody body = ResponseBody.readFromStream(channel, bodyLen);
    return Response.builder().header(ResponseHeader.fromRaw(rawHeader)).body(body).build();
  }

  /**
   * Serialise response and write it to given stream.
   *
   * <p>Header is converted to a raw format before serializing.
   *
   * <p># Errors - if writing any of the subfields (header or body) fails, then
   * `ResponseStatus::ConnectionError` is returned. - if encoding any of the fields in the header
   * fails, then `ResponseStatus::InvalidEncoding` is returned.
   */
  public void writeToStream(WritableByteChannel channel) throws IOException {
    header.toRaw().bodyLen(body.length()).build().writeToStream(channel);
    body.writeToStream(channel);
  }
}
