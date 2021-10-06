package org.parallaxsecond.jna;

import lombok.Builder;
import lombok.Value;

import java.util.Locale;

@Builder(builderMethodName = "value")
@Value
public class Platform<T> {
  T linux;
  T linuxAMD64;
  T linuxAARCH64;
  T osx;
  T osxAMD64;
  T osxAARCH64;

  T fallback;

  public static boolean isLinux() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return os.contains("nux");
  }

  public static boolean isAmd64() {
    return "amd64".equals(System.getProperty("os.arch"))
        || "x86_64".equals(System.getProperty("os.arch"));
  }

  public static boolean isAarch64() {
    return "aarch64".equals(System.getProperty("os.arch"))
        || "arm64".equals(System.getProperty("os.arch"));
  }

  public static boolean isOsx() {
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    return ((os.contains("mac")) || (os.contains("darwin")));
  }

  @SuppressWarnings({"unchecked", "unused"})
  public static class PlatformBuilder<T> {
    public <V> V get() {
      if (linuxAMD64 != null && isLinux() && isAmd64()) {
        return (V) linuxAMD64;
      }
      if (linuxAARCH64 != null && isLinux() && isAarch64()) {
        return (V) linuxAARCH64;
      }
      if (linux != null && isLinux()) {
        return (V) linux;
      }
      if (osxAMD64 != null && isOsx() && isAmd64()) {
        return (V) osxAMD64;
      }
      if (osxAARCH64 != null && isOsx() && isAarch64()) {
        return (V) osxAARCH64;
      }
      if (osx != null && isOsx()) {
        return (V) osx;
      }
      if (fallback == null) {
        throw new IllegalStateException("no platform matches and no fallback provided");
      }
      return (V) fallback;
    }
  }
}
