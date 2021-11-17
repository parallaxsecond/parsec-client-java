package org.parallaxsecond.parsec.jce.provider;

import java.security.SecureRandomSpi;

import org.parallaxsecond.parsec.client.core.BasicClient;

/**
 *
 */
public final class SecureRandomParsec extends SecureRandomSpi {

  BasicClient client;

  public SecureRandomParsec() {
    this.client = ParsecProvider.getBasicClient();
  }

  @Override
  protected void engineSetSeed(byte[] seed) {
    // TODO: verify if this is the intended behaviour
    // throw new UnsupportedOperationException("Parsec does not accept seeding the random number generator.");
  }

  @Override
  protected void engineNextBytes(byte[] bytes) {
    byte[] parsecBytes = client.psaGenerateRandom(bytes.length);
    System.arraycopy(parsecBytes, 0, bytes, 0, parsecBytes.length);
  }

  @Override
  protected byte[] engineGenerateSeed(int numBytes) {
    // TODO: Verify that we can simply use `psaGenerateRandom` here (The use case may be to generate seeds for other RNGs)
    byte[] seed = new byte[numBytes];
    engineNextBytes(seed);
    return seed;
  }
}
