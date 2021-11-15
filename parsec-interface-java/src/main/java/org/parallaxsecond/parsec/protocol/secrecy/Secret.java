package org.parallaxsecond.parsec.protocol.secrecy;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class Secret<T, N> {
  protected final N nullValue;
  @Getter protected final T value;

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    zeroize();
  }

  public void zeroize() {}

  public static class SecretBytes extends Secret<byte[], Byte> {
    public SecretBytes(@NonNull byte[] value) {
      super((byte) 0, Arrays.copyOf(value, value.length));
    }

    public void zeroize() {
      if (value == null) {
        return;
      }
      Arrays.fill(value, nullValue);
    }

    public int length() {
      return value.length;
    }
  }
}
