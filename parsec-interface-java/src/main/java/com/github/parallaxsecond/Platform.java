package com.github.parallaxsecond;

import lombok.Builder;
import lombok.Value;

import java.util.Locale;

@Builder(builderMethodName = "value")
@Value
public class Platform<T> {
  T linux;
  T osx;
  T fallback;

  public static boolean isLinux() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return os.contains("nux");
  }

  public static boolean isOsx() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return ((os.contains("mac")) || (os.contains("darwin")));
  }

  public static class PlatformBuilder<T> {
    public <T> T get() {
      if (linux != null && isLinux()) {
        return (T) linux;
      }
      if (osx != null && isOsx()) {
        return (T) osx;
      }
      if (fallback == null) {
        throw new IllegalStateException("no platform matches and no fallback provided");
      }
      return (T) fallback;
    }
  }
}
