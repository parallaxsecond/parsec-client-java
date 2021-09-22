package com.github.parallaxsecond.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface Uid extends Library {
  Uid IMPL = Native.load("c", Uid.class);

  int getuid();

  static int getUid() {
    return IMPL.getuid();
  }
}
