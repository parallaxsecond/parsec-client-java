package org.parallaxsecond.parsec.jca;

import java.security.SecureRandomSpi;
import org.parallaxsecond.core.BasicClient;

/**
 *
 */
public class SecureRandomParsec extends SecureRandomSpi {

  BasicClient client;

  public SecureRandomParsec(BasicClient client){
    this.client = client;
  }

  @Override
  protected void engineSetSeed(byte[] seed) {
      // TODO: verify if this is the intended behaviour
      throw new UnsupportedOperationException("Parsec does not support seeding the random number generator.");
  }

  @Override
  protected void engineNextBytes(byte[] bytes) {
    byte[] parsecBytes = client.psaGenerateRandom(bytes.length);
    System.arraycopy(parsecBytes, 0, bytes, 0, parsecBytes.length); ;
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes) {
    // TODO: verify if this is the intended behaviour
    return client.psaGenerateRandom(numBytes);
  }
}
