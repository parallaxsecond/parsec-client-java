package org.parallaxsecond.parsec.jce.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public final class DynamicByteBuffer implements DynamicByteBufferWriter {
  @Getter
  @Delegate(excludes = {DynamicByteBufferWriter.class, Buffer.class})
  private ByteBuffer byteBuffer;
  @Delegate(excludes = DynamicByteBufferWriter.class)
  private Buffer buffer;
  @Getter private final float growFactor;
  public DynamicByteBuffer(int initialCapacity, float growFactor) {
    if (growFactor <= 1) {
      throw new IllegalArgumentException("growFactor must be > 1");
    }
    this.growFactor = growFactor;
    this.byteBuffer = ByteBuffer.allocate(initialCapacity);
  }
  public void setByteBuffer(ByteBuffer buffer) {
    this.byteBuffer = buffer;
    this.buffer = buffer;
  }
}
