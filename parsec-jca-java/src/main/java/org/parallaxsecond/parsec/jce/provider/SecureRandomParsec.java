package org.parallaxsecond.parsec.jce.provider;

import lombok.RequiredArgsConstructor;

import java.security.SecureRandomSpi;

@RequiredArgsConstructor
public final class SecureRandomParsec extends SecureRandomSpi {
  private final ParsecClientAccessor parsecClientFactory;

  @Override
  protected void engineSetSeed(byte[] seed) {
    // TODO: verify if this is the intended behaviour
    // throw new UnsupportedOperationException("Parsec does not accept seeding the random number generator.");
  }

  @Override
  protected void engineNextBytes(byte[] bytes) {
    byte[] parsecBytes = parsecClientFactory.get().psaGenerateRandom(bytes.length);
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
