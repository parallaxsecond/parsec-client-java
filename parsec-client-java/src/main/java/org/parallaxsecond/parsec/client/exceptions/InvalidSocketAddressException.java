package org.parallaxsecond.parsec.client.exceptions;

import org.parallaxsecond.parsec.client.core.FileStat;

import java.nio.file.Files;
import java.nio.file.Path;

/** The socket address provided is not valid */
public class InvalidSocketAddressException extends ClientException {
  public InvalidSocketAddressException(Path path) {
    super(
        "the socket address provided in the URL is not valid: "
            + path
            + " exists: "
            + Files.exists(path)
            + ", isSocket: "
            + FileStat.isSocket(path),
        null);
  }
}
