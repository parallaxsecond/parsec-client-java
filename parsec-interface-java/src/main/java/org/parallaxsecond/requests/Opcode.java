package org.parallaxsecond.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.stream.Stream;

/**
 * Listing of available operations and their associated opcode.
 *
 * <p>Passed in headers as `opcode`. Check the
 * [Operations](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/index.html)
 * page of the book for more information.
 */
@Getter
@RequiredArgsConstructor
public enum Opcode {
  /** Ping operation */
  PING(0x0001, true, false),
  /** PsaGenerateKey operation */
  PSA_GENERATE_KEY(0x0002, false, false),
  /** PsaDestroyKey operation */
  PSA_DESTROY_KEY(0x0003, false, false),
  /** PsaSignHash operation */
  PSA_SIGN_HASH(0x0004, false, false),
  /** PsaVerifyHash operation */
  PSA_VERIFY_HASH(0x0005, false, false),
  /** PsaImportKey operation */
  PSA_IMPORT_KEY(0x0006, false, false),
  /** PsaExportPublicKey operation */
  PSA_EXPORT_PUBLIC_KEY(0x0007, false, false),
  /** ListProviders operation */
  LIST_PROVIDERS(0x0008, false, false),
  /** ListOpcodes operation */
  LIST_OPCODES(0x0009, true, false),
  /** PsaAsymmetricEncrypt operation */
  PSA_ASYMMETRIC_ENCRYPT(0x000A, false, false),
  /** PsaAsymmetricDecrypt operation */
  PSA_ASYMMETRIC_DECRYPT(0x000B, false, false),
  /** PsaExportKey operation */
  PSA_EXPORT_KEY(0x000C, false, false),
  /** PsaGenerateRandom operation */
  PSA_GENERATE_RANDOM(0x000D, false, false),
  /** ListAuthenticators operation */
  LIST_AUTHENTICATORS(0x000E, true, false),
  /** PsaHashCompute operation */
  PSA_HASH_COMPUTE(0x000F, false, false),
  /** PsaHashCompare operation */
  PSA_HASH_COMPARE(0x0010, false, false),
  /** PsaAeadEncrypt */
  PSA_AEAD_ENCRYPT(0x0011, false, false),
  /** PsaAeadDecrypt */
  PSA_AEAD_DECRYPT(0x0012, false, false),
  /** PsaRawKeyAgreement operation */
  PSA_RAW_KEY_AGREEMENT(0x0013, false, false),
  /** PsaSignMessage operation */
  PSA_SIGN_MESSAGE(0x0018, false, false),
  /** PsaVerifyMessage operation */
  PSA_VERIFY_MESSAGE(0x0019, false, false),
  /** ListKeys operation */
  LIST_KEYS(0x001A, true, false),
  /** ListClients operation (admin operation) */
  LIST_CLIENTS(0x001B, true, true),
  /** DeleteClient operation (admin operation) */
  DELETE_CLIENT(0x001C, true, true);

  private final int code;
  private final boolean core;
  private final boolean admin;

  public static Opcode fromCode(int opcode) {
    return Stream.of(values())
        .filter(v -> v.getCode() == opcode)
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }

  public boolean isCrypto() {
    return !isCore();
  }
}
