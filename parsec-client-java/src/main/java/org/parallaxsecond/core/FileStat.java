package org.parallaxsecond.core;

import lombok.SneakyThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

public interface FileStat {

  @SneakyThrows
  static boolean isSocket(Path file) {
    return file != null && Files.readAttributes(file, BasicFileAttributes.class).isOther();
  }
}
