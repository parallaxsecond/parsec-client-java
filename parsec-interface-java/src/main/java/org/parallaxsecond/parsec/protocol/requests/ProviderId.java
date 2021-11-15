package org.parallaxsecond.parsec.protocol.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Listing of provider types and their associated codes.
 *
 * <p>Passed in headers as `provider`.
 */
@RequiredArgsConstructor
public enum ProviderId {
  /** Provider to use for core Parsec operations. */
  CORE((byte) 0, "Core provider"),
  /** Provider using Mbed Crypto software library. */
  MBED_CRYPTO((byte) 1, "Mbed Crypto provider"),
  /** Provider using a PKCS 11 compatible library. */
  PKCS11((byte) 2, "PKCS #11 provider"),
  /** Provider using a TSS 2.0 Enhanced System API library. */
  TPM((byte) 3, "TPM provider"),
  /** Provider using the crypto Trusted Service running in TrustZone */
  TRUSTED_SERVICE((byte) 4, "Trusted Service provider"),
  /** Provider using the MicrochipTech cryptodevice ATECCx08 via CryptoAuthentication Library */
  CRYPTO_AUTH_LIB((byte) 5, "CryptoAuthentication Library provider");
  @Getter private final byte id;
  private final String description;

  public static ProviderId fromCode(byte provider) {
    return values()[provider];
  }

  @Override
  public String toString() {
    return description;
  }
}
