package org.parallaxsecond.parsec.jce.provider;

import java.nio.ByteBuffer;

/** Note that all put operations return null, since chaining wouldn't work. */
@SuppressWarnings("unused")
public interface DynamicByteBufferWriter {

  default ByteBuffer put(byte b) {
    ensureCapacity(1).put(b);
    return null;
  }

  default ByteBuffer put(byte[] src) {
    ensureCapacity(src.length).put(src);
    return null;
  }

  default ByteBuffer put(byte[] src, int offset, int length) {
    ensureCapacity(length).put(src, offset, length);
    return null;
  }

  default ByteBuffer put(ByteBuffer src) {
    ensureCapacity(src.remaining()).put(src);
    return null;
  }

  default ByteBuffer put(int index, byte b) {
    ensureCapacity(1).put(index, b);
    return null;
  }

  default ByteBuffer putChar(char value) {
    ensureCapacity(2).putChar(value);
    return null;
  }

  default ByteBuffer putChar(int index, char value) {
    ensureCapacity(2).putChar(index, value);
    return null;
  }

  default ByteBuffer putDouble(double value) {
    ensureCapacity(8).putDouble(value);
    return null;
  }

  default ByteBuffer putDouble(int index, double value) {
    ensureCapacity(8).putDouble(index, value);
    return null;
  }

  default ByteBuffer putFloat(float value) {
    ensureCapacity(4).putFloat(value);
    return null;
  }

  default ByteBuffer putFloat(int index, float value) {
    ensureCapacity(4).putFloat(index, value);
    return null;
  }

  default ByteBuffer putInt(int value) {
    ensureCapacity(4).putInt(value);
    return null;
  }

  default ByteBuffer putInt(int index, int value) {
    ensureCapacity(4).putInt(index, value);
    return null;
  }

  default ByteBuffer putLong(int index, long value) {
    ensureCapacity(8).putLong(index, value);
    return null;
  }

  default ByteBuffer putLong(long value) {
    ensureCapacity(8).putLong(value);
    return null;
  }

  default ByteBuffer putShort(int index, short value) {
    ensureCapacity(2).putShort(index, value);
    return null;
  }

  default ByteBuffer putShort(short value) {
    ensureCapacity(2).putShort(value);
    return null;
  }

  default ByteBuffer ensureCapacity(int needed) {
    if (getByteBuffer().remaining() >= needed) {
      return getByteBuffer();
    }
    int newCapacity = (int) (getByteBuffer().capacity() * getGrowFactor());
    while (newCapacity < (getByteBuffer().capacity() + needed)) {
      newCapacity *= getGrowFactor();
    }
    ByteBuffer expanded = ByteBuffer.allocate(newCapacity);
    expanded.order(getByteBuffer().order());
    expanded.put(getByteBuffer());
    setByteBuffer(expanded);
    return getByteBuffer();
  }

  ByteBuffer getByteBuffer();

  void setByteBuffer(ByteBuffer expanded);

  float getGrowFactor();
}
