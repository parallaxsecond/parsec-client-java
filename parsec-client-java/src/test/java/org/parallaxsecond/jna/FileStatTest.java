package org.parallaxsecond.jna;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class FileStatTest {

  @Test
  @SneakyThrows
  public void testIsSocket() {
    Path socketFile = Files.createTempFile("sock", "sock");
    Files.delete(socketFile);
    int socket = UnixSocket.unixSocket();
    try {
      assertFalse(Files.exists(socketFile));
      UnixSocket.bind(socket, socketFile.toFile().getAbsolutePath());
      assertTrue(Files.exists(socketFile));
      assertTrue(FileStat.isSocket(socketFile));
    } finally {
      UnixSocket.closeSocket(socket);
    }
  }
}
