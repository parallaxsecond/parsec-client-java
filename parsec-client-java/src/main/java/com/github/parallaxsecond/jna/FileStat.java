package com.github.parallaxsecond.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.function.Function;

/**
 * Use something like the snippet below to get the platform specific offset
 *
 * <pre>
 * gcc -x c - << EOF
 * #include &lt;stddef.h>
 * #include &lt;stdio.h>
 * #include &lt;sys/stat.h>
 *
 * int main(int argc, char** argv) {
 * printf("st_mode offset %d\n", (int) offsetof(struct stat, st_mode));
 * printf("mode_t size %d\n", (int) sizeof(mode_t));
 * return 0;
 * }
 * EOF
 * ./a.out
 * </pre>
 */
public interface FileStat extends Library {
  FileStat IMPL = Native.load("c", FileStat.class);
  int STAT_STRUCT_LEN = 300; // use large buffer normally 144
  int ST_MODE_POSITION =
      Platform.value().linuxAMD64(24).linuxAARCH64(16).osxAMD64(8).osxAARCH64(4).get();
  //
  Function<ByteBuffer, Integer> ST_MODE_EXTRACTOR =
      Platform.<Function<ByteBuffer, Integer>>value()
          .linux(buf -> ((ByteBuffer) buf.position(ST_MODE_POSITION)).getInt())
          .osx(buf -> ((int) ((ByteBuffer) buf.position(ST_MODE_POSITION)).getShort()))
          .get();

  @SneakyThrows
  static boolean isSocket(String filename) {
    ByteBuffer buf = ByteBuffer.allocate(STAT_STRUCT_LEN).order(ByteOrder.nativeOrder());
    Error.throwError(IMPL.lstat(filename, buf));
    int stMode = ST_MODE_EXTRACTOR.apply(buf);
    return (stMode & 0x0000F000) == 0xC000;
  }

  int lstat(String path, ByteBuffer buffer);
}
