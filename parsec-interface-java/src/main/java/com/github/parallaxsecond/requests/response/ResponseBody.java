package com.github.parallaxsecond.requests.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
@Getter
@Slf4j
public class ResponseBody {
  private final ByteBuffer buffer;

  public ResponseBody(byte[] buffer) {
    this(ByteBuffer.wrap(buffer));
  }

  public void writeToStream(WritableByteChannel channel) throws IOException {
    channel.write(buffer);
  }

  public static ResponseBody readFromStream(ReadableByteChannel channel, int len)
      throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(len).order(ByteOrder.LITTLE_ENDIAN);
    int read = channel.read(buf);
    if (read != len) {
      log.warn("expected to read {} but only got {}", len, read);
    }
    buf.flip();
    buf.limit(Math.min(len, read));

    return new ResponseBody(buf.slice());
  }

  public int length() {
    return buffer.remaining();
  }
}
