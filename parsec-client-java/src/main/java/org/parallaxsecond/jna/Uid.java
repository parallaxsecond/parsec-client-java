package org.parallaxsecond.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.util.concurrent.atomic.AtomicReference;

public interface Uid extends Library {
  AtomicReference<Uid> IMPL = new AtomicReference<>(Native.load("c", Uid.class));

  static int getUid() {
    return IMPL.get().getuid();
  }

  int getuid();
}
