package org.parallaxsecond.parsec.protocol.requests.request;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RequestBody {
  @Getter
  private final ByteBuffer buffer;

  public RequestBody(byte[] buf) {
    this(ByteBuffer.wrap(buf));
  }

  public static RequestBody readFromStream(ReadableByteChannel channel, int len)
      throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
    channel.read(buf);
    ((Buffer) buf).flip();
    return new RequestBody(buf);
  }

  public void writeToStream(WritableByteChannel channel) throws IOException {
    channel.write(buffer);
  }

  public int length() {
    return buffer.remaining();
  }

  @Override
  public String toString() {
    return "RequestBody current length=" + length();
  }
}
