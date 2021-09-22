package com.github.parallaxsecond.operations;

import com.github.parallaxsecond.requests.AuthType;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.ProviderId;
import com.github.parallaxsecond.secrecy.Secret;
import lombok.Builder;
import lombok.Value;
import psa_key_attributes.PsaKeyAttributes;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Container type for result conversion values, holding a native result object to be */
/** passed in/out of the converter. */
public interface NativeResult {
  Opcode getOpcode();

  /** ListProviders result */
  @Value
  @Builder
  class ListProvidersResult implements NativeResult {

    /** Structure holding the basic information that defines the providers in */
    Opcode opcode = Opcode.LIST_PROVIDERS;
    List<ProviderInfo> providers;

    /** the service for client discovery. */
    @Value
    @Builder
    public static class ProviderInfo {
      /** Unique, permanent, identifier of the provider. */
      UUID uuid;
      /** Short description of the provider. */
      String description;
      /** Provider vendor. */
      String vendor;
      /** Provider implementation version major. */
      int versionMaj;
      /** Provider implementation version minor. */
      int versionMin;
      /** Provider implementation version revision number. */
      int versionRev;
      /** Provider ID to use on the wire protocol to communicate with this provider. */
      ProviderId id;
    }
  }
  /** ListOpcodes result */
  @Value
  @Builder
  class ListOpcodesResult implements NativeResult {
    Opcode opcode = Opcode.LIST_OPCODES;
    /** `opcodes` holds a list of opcodes supported by the provider identified in the request. */
    Set<Opcode> opcodes;
  }
  /** ListAuthenticators result */
  @Value
  @Builder
  class ListAuthenticatorsResult implements NativeResult {
    Opcode opcode = Opcode.LIST_AUTHENTICATORS;
    List<AuthenticatorInfo> authenticators;

    /**
     * Structure holding the basic information that defines the authenticators in the service for
     * client discovery.
     */
    @Value
    @Builder
    public static class AuthenticatorInfo {
      /** Short description of the authenticator. */
      String description;
      /** Authenticator implementation version major. */
      int versionMaj;
      /** Authenticator implementation version minor. */
      int versionMin;
      /** Authenticator implementation version revision number. */
      int versionRev;
      /** Authenticator ID to use on the wire protocol to communicate with this authenticator. */
      AuthType id;
    }
  }
  /** ListKeys result */
  @Value
  @Builder
  class ListKeysResult implements NativeResult {
    Opcode opcode = Opcode.LIST_KEYS;
    /** A list of `KeyInfo` structures. */
    List<KeyInfo> keys;

    /**
     * Structure holding the basic information for a key in the application for client discovery.
     */
    @Value
    @Builder
    public static class KeyInfo {
      /** The ID of the associated provider. */
      ProviderId providerId;
      /** The name of the key. */
      String name;
      /** The key attributes. */
      // FIXME
      PsaKeyAttributes attributes;
    }
  }
  /** ListClients result */
  @Value
  @Builder
  class ListClientsResult implements NativeResult {
    Opcode opcode = Opcode.LIST_CLIENTS;
    /** A list of client application names. */
    List<String> clients;
  }
  /** DeleteClient result */
  @Value
  @Builder
  class DeleteClientResult implements NativeResult {
    Opcode opcode = Opcode.DELETE_CLIENT;
  }
  /** Ping result */
  @Value
  @Builder
  class PingResult implements NativeResult {
    Opcode opcode = Opcode.PING;
    /** Supported latest wire protocol version major */
    byte wireProtocolVersionMaj;
    /** Supported latest wire protocol version minor */
    byte wireProtocolVersionMin;
  }
  /** PsaGenerateKey result */
  @Value
  @Builder
  class PsaGenerateKeyResult implements NativeResult {
    Opcode opcode = Opcode.PSA_GENERATE_KEY;
  }
  /** PsaImportKey result */
  @Value
  @Builder
  class PsaImportKeyResult implements NativeResult {
    Opcode opcode = Opcode.PSA_IMPORT_KEY;
  }
  /** PsaExportPublicKey result */
  @Value
  @Builder
  class PsaExportPublicKeyResult implements NativeResult {
    Opcode opcode = Opcode.PSA_EXPORT_PUBLIC_KEY;
    /**
     * `data` holds the bytes defining the public key, formatted as specified by the provider for
     * which the request was made.
     */
    byte[] data;
  }
  /** PsaExportKey result */
  @Value
  @Builder
  class PsaExportKeyResult implements NativeResult {
    Opcode opcode = Opcode.PSA_EXPORT_KEY;
    /**
     * `data` holds the bytes defining the key, formatted as specified by the provider for which the
     * request was made.
     */
    byte[] data;
  }
  /** PsaDestroyKey result */
  @Value
  @Builder
  class PsaDestroyKeyResult implements NativeResult {
    Opcode opcode = Opcode.PSA_DESTROY_KEY;
  }
  /** PsaSignHash result */
  @Value
  @Builder
  class PsaSignHashResult implements NativeResult {
    Opcode opcode = Opcode.PSA_SIGN_HASH;
    /**
     * The `signature` field contains the resulting bytes from the signing operation. The format of
     * the signature is as specified by the provider doing the signing.
     */
    byte[] signature;
  }
  /** PsaHashCompute result */
  @Value
  @Builder
  class PsaHashComputeResult implements NativeResult {
    Opcode opcode = Opcode.PSA_HASH_COMPUTE;
    /** The `hash` field contains the hash of the message. */
    byte[] hash;
  }
  /** PsaHashCompare result */
  @Value
  @Builder
  class PsaHashCompareResult implements NativeResult {
    Opcode opcode = Opcode.PSA_HASH_COMPARE;
  }
  /** PsaVerifyHash result */
  @Value
  @Builder
  class PsaVerifyHashResult implements NativeResult {
    Opcode opcode = Opcode.PSA_VERIFY_HASH;
  }
  /** PsaAsymmetricEncrypt result */
  @Value
  @Builder
  class PsaAsymmetricEncryptResult implements NativeResult {
    Opcode opcode = Opcode.PSA_ASYMMETRIC_ENCRYPT;
    /** The `ciphertext` field contains the encrypted short message. */
    byte[] ciphertext;
  }
  /** PsaAsymmetricDecrypt result */
  @Value
  @Builder
  class PsaAsymmetricDecryptResult implements NativeResult {
    Opcode opcode = Opcode.PSA_ASYMMETRIC_DECRYPT;
    /** Decrypted message */
    byte[] plaintext;
  }
  /** PsaAeadEncrypt result */
  @Value
  @Builder
  class PsaAeadEncryptResult implements NativeResult {
    Opcode opcode = Opcode.PSA_AEAD_ENCRYPT;
    /**
     * The `ciphertext` field contains the encrypted and authenticated data.For algorithms where the
     * encrypted data and the authentication tag are defined as separate outputs, the authentication
     * tag is appended to the encrypted data.
     */
    byte[] ciphertext;
  }
  /** PsaAeadDecrypt result */
  @Value
  @Builder
  class PsaAeadDecryptResult implements NativeResult {
    Opcode opcode = Opcode.PSA_AEAD_DECRYPT;

    /** The `plaintext` field contains the authenticated and decrypted data. */
    byte[] plaintext;
  }
  /** PsaGenerateRandom result */
  @Value
  @Builder
  class PsaGenerateRandomResult implements NativeResult {
    Opcode opcode = Opcode.PSA_GENERATE_RANDOM;
    /** Random bytes. */
    byte[] randomBytes;
  }
  /** PsaRawKeyAgreement result */
  @Value
  @Builder
  class PsaRawKeyAgreementResult implements NativeResult {
    Opcode opcode = Opcode.PSA_RAW_KEY_AGREEMENT;
    /**
     * `data` holds the bytes defining the key, formatted as specified by the provider for which the
     * request was made.
     */
    Secret.SecretBytes sharedSecret;
  }
  /** PsaSignMessage result */
  @Value
  @Builder
  class PsaSignMessageResult implements NativeResult {
    Opcode opcode = Opcode.PSA_SIGN_MESSAGE;
    /**
     * The `signature` field contains the resulting bytes from the signing operation. The format of
     * the signature is as specified by the provider doing the signing.
     */
    byte[] signature;
  }
  /** PsaVerifyMessage result */
  @Value
  @Builder
  class PsaVerifyMessageResult implements NativeResult {
    Opcode opcode = Opcode.PSA_VERIFY_MESSAGE;
  }
}
