package org.parallaxsecond.operations;

import org.parallaxsecond.requests.Opcode;
import org.parallaxsecond.requests.ProviderId;
import lombok.Builder;
import lombok.Value;
import psa_algorithm.PsaAlgorithm;
import psa_key_attributes.PsaKeyAttributes;
import psa_raw_key_agreement.PsaRawKeyAgreement;

/**
 * # Rust representation of operations
 *
 * <p>Rust native representation of the language neutral operations described in the
 * [Operations](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/index.html)
 * page in the book. Some of the doc comments have directly been taken from the PSA Crypto API
 * document version 1.0.0. Please check that
 * [document](https://developer.arm.com/architectures/security-architectures/platform-security-architecture/documentation)
 * and the book for more details.
 *
 * <p>Container type for operation conversion values, holding a native operation object to be passed
 * in/out of a converter.
 */
public interface NativeOperation {
  Opcode getOpcode();

  /** ListProviders operation */
  @Value
  @Builder
  class ListProvidersOperation implements NativeOperation {
    Opcode opcode = Opcode.LIST_PROVIDERS;
  }
  /** ListOpcodes operation */
  @Value
  @Builder
  class ListOpcodesOperation implements NativeOperation {
    Opcode opcode = Opcode.LIST_OPCODES;
    ProviderId providerId;
  }
  /** ListAuthenticators operation */
  @Value
  @Builder
  class ListAuthenticatorsOperation implements NativeOperation {
    Opcode opcode = Opcode.LIST_AUTHENTICATORS;
  }
  /** ListKeys operation */
  @Value
  @Builder
  class ListKeysOperation implements NativeOperation {
    Opcode opcode = Opcode.LIST_KEYS;
  }
  /** ListClients operation */
  @Value
  @Builder
  class ListClientsOperation implements NativeOperation {
    Opcode opcode = Opcode.LIST_CLIENTS;
  }
  /** DeleteClient operation */
  @Value
  @Builder
  class DeleteClientOperation implements NativeOperation {
    Opcode opcode = Opcode.DELETE_CLIENT;
    /** A client application name. */
    String client;
  }
  /** Ping operation */
  @Value
  @Builder
  class PingOperation implements NativeOperation {
    Opcode opcode = Opcode.PING;
  }
  /** PsaGenerateKey operation */
  @Value
  @Builder
  class PsaGenerateKeyOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_GENERATE_KEY;
    /** `key_name` specifies a name by which the service will identify the key. Key */
    /** name must be unique per application. */
    String keyName;
    /** `attributes` specifies the parameters to be associated with the key. */
    // FIXME
    PsaKeyAttributes attributes;
  }
  /** PsaImportKey operation */
  @Value
  @Builder
  class PsaImportKeyOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_IMPORT_KEY;
    /**
     * `key_name` specifies a name by which the service will identify the key. Key name must be
     * unique per application.
     */
    String keyName;
    /** `attributes` specifies the attributes for the new key. */
    // FIXME
    PsaKeyAttributes attributes;
    /**
     * `data` contains the bytes for the key, formatted in accordance with the requirements of the
     * provider for the key type specified in `attributes`. Debug is not derived for this because it
     * could expose secrets if printed or logged somewhere
     */
    // FIXME Use secretBytes ...
    byte[] data;
  }
  /** PsaExportPublicKey operation */
  @Value
  @Builder
  class PsaExportPublicKeyOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_EXPORT_PUBLIC_KEY;
    /**
     * `key_name` identifies the key for which the public part will be exported. The specified key
     * must be an asymmetric keypair.
     */
    String keyName;
  }
  /** PsaExportKey operation */
  @Value
  @Builder
  class PsaExportKeyOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_EXPORT_KEY;
    /** `key_name` identifies the key that will be exported. */
    String keyName;
  }
  /** PsaDestroyKey operation */
  @Value
  @Builder
  class PsaDestroyKeyOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_DESTROY_KEY;
    /** `key_name` identifies the key to be destroyed. */
    String keyName;
  }
  /** PsaSignHash operation */
  @Value
  @Builder
  class PsaSignHashOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_SIGN_HASH;
    /** Defines which key should be used for the signing operation. */
    String keyName;
    /**
     * An asymmetric signature algorithm that separates the hash and sign operations, that is
     * compatible with the type of key
     */
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricSignature alg;
    /** The input whose signature is to be verified. This is usually the hash of a message. */
    byte[] hash;
  }
  /** PsaVerifyHash operation */
  @Value
  @Builder
  class PsaVerifyHashOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_VERIFY_HASH;
    /** `key_name` specifies the key to be used for verification. */
    String keyName;
    /**
     * An asymmetric signature algorithm that separates the hash and sign operations, that is
     * compatible with the type of key.
     */
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricSignature alg;
    /**
     * The `hash` contains a short message or hash value as described for the asymmetric signing
     * operation.
     */
    byte[] hash;
    /** Buffer containing the signature to verify. */
    byte[] signature;
  }
  /** PsaHashCompute operation */
  @Value
  @Builder
  class PsaHashComputeOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_HASH_COMPUTE;
    /** The hash algorithm to compute. */
    // FIXME
    PsaAlgorithm.Algorithm.Hash alg;
    /** The input to hash. */
    byte[] input;
  }
  /** PsaHashCompare operation */
  @Value
  @Builder
  class PsaHashCompareOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_HASH_COMPARE;
    /** The hash algorithm to compute. */
    // FIXME
    PsaAlgorithm.Algorithm.Hash alg;
    /** The input to hash. */
    byte[] input;
    /** The reference hash value. */
    byte[] hash;
  }

  /** PsaAsymmetricEncrypt operation */
  @Value
  @Builder
  class PsaAsymmetricEncryptOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_ASYMMETRIC_ENCRYPT;
    /** Defines which key should be used for the encryption operation. */
    String keyName;
    /** An asymmetric encryption algorithm that is compatible with the key type */
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricEncryption alg;
    /** The short message to be encrypted. */
    byte[] plaintext;
    /** Salt to use during encryption, if supported by the algorithm. */
    byte[] salt;
  }
  /** PsaAsymmetricDecrypt operation */
  @Value
  @Builder
  class PsaAsymmetricDecryptOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_ASYMMETRIC_DECRYPT;
    /** Defines which key should be used for the signing operation. */
    String keyName;
    /** An asymmetric encryption algorithm to be used for decryption, that is compatible with the */
    // type of key.
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricEncryption alg;
    /** The short encrypted message to be decrypted. */
    byte[] ciphertext;
    /** Salt to use during decryption, if supported by the algorithm. */
    byte[] salt;
  }
  /** PsaAeadEncrypt operation */
  @Value
  @Builder
  class PsaAeadEncryptOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_AEAD_ENCRYPT;
    /** Defines which key should be used for the encryption operation. */
    String keyName;
    /** An AEAD encryption algorithm that is compatible with the key type. */
    // FIXME
    PsaAlgorithm.Algorithm.Aead alg;
    /** Nonce or IV to use. */
    byte[] nonce;
    /** Additional data that will be authenticated but not encrypted. */
    byte[] additionalData;
    /** Data that will be authenticated and encrypted. */
    byte[] plaintext;
  }

  /** PsaAeadDecrypt operation */
  @Value
  @Builder
  class PsaAeadDecryptOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_AEAD_DECRYPT;
    /** Defines which key should be used for the decryption operation. */
    String keyName;
    /** An AEAD encryption algorithm that is compatible with the key type. */
    // FIXME
    PsaAlgorithm.Algorithm.Aead alg;

    /** Nonce or IV to use. */
    byte[] nonce;

    /** Additional data that has been authenticated but not encrypted. */
    byte[] additionalData;

    /**
     * Data that has been authenticated and encrypted. For algorithms where the encrypted data and
     * the authentication tag are defined as separate inputs, the buffer must contain the encrypted
     * data followed by the authentication tag.
     */
    byte[] ciphertext;
  }
  /** PsaGenerateRandom operation */
  @Value
  @Builder
  class PsaGenerateRandomOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_GENERATE_RANDOM;
    /** `size` specifies how many random bytes to fetch. */
    long size;
  }
  /** PsaRawKeyAgreement operation */
  @Value
  @Builder
  class PsaRawKeyAgreementOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_RAW_KEY_AGREEMENT;
    /**
     * `alg` specifies the raw key agreement algorithm to use. It must allow the `derive` usage
     * flag.
     */
    // FIXME
    PsaRawKeyAgreement alg; // : RawKeyAgreement,
    /**
     * `private_key_name` specifies a name of the private key to use in the key agreement operation.
     */
    String privateKeyName;
    /**
     * `peer_key` contains the bytes of a peers public key, to be used in the key agreement
     * operation. This must be in the format that `PsaImportKey` accepts.
     */
    byte[] peerKey;
  }
  /** PsaSignMessage operation */
  @Value
  @Builder
  class PsaSignMessageOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_SIGN_MESSAGE;
    /** Defines which key should be used for the signing operation. */
    String keyName;
    /**
     * An asymmetric signature algorithm that separates the hash and sign operations, that is
     * compatible with the type of key.
     */
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricSignature alg;
    /** The message to sign. */
    byte[] message;
  }
  /** PsaVerifyMessage operation */
  @Value
  @Builder
  class PsaVerifyMessageOperation implements NativeOperation {
    Opcode opcode = Opcode.PSA_VERIFY_MESSAGE;
    /** `key_name` specifies the key to be used for verification. */
    String keyName;
    /**
     * An asymmetric signature algorithm that separates the hash and sign operations, that is
     * compatible with the type of key.
     */
    // FIXME
    PsaAlgorithm.Algorithm.AsymmetricSignature alg;
    /** The `message` whose signature is to be verified for the asymmetric signing operation. */
    byte[] message;
    /** Buffer containing the signature to verify. */
    byte[] signature;
  }
}
