package org.parallaxsecond.parsec.jce.provider;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Delegate;

import java.nio.ByteBuffer;

public final class DynamicByteBuffer implements DynamicByteBufferWriter {
  @Getter
  @Setter
  @Delegate(excludes = DynamicByteBufferWriter.class)
  private ByteBuffer byteBuffer;
  @Getter private final float growFactor;
  public DynamicByteBuffer(int initialCapacity, float growFactor) {
    if (growFactor <= 1) {
      throw new IllegalArgumentException("growFactor must be > 1");
    }
    this.growFactor = growFactor;
    this.byteBuffer = ByteBuffer.allocate(initialCapacity);
  }
}
