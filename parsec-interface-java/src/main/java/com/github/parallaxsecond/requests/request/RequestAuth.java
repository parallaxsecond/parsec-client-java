package com.github.parallaxsecond.requests.request;

import com.github.parallaxsecond.secrecy.Secret;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

@RequiredArgsConstructor
public class RequestAuth {
  @Getter private final Secret.SecretBytes buffer;

  public RequestAuth() {
    this(new byte[0]);
  }

  public RequestAuth(byte[] bytes) {
    this.buffer = new Secret.SecretBytes(bytes);
  }

  public static RequestAuth readFromStream(ReadableByteChannel channel, short authLen)
      throws IOException {
    ByteBuffer buf = ByteBuffer.allocate(authLen).order(ByteOrder.LITTLE_ENDIAN);
    channel.read(buf);
    return new RequestAuth(buf.array());
  }

  public void writeToStream(WritableByteChannel channel) throws IOException {
    channel.write(ByteBuffer.wrap(buffer.getValue()));
  }
}
