package org.parallaxsecond.parsec.client.core;

import org.parallaxsecond.parsec.client.Authentication;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import org.parallaxsecond.parsec.client.exceptions.*;
import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.ProviderId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.parallaxsecond.parsec.protobuf.psa_algorithm.PsaAlgorithm;
import org.parallaxsecond.parsec.protobuf.psa_key_attributes.PsaKeyAttributes;
import org.parallaxsecond.parsec.protobuf.psa_raw_key_agreement.PsaRawKeyAgreement;

import java.time.Duration;

import static java.util.Optional.ofNullable;

/**
 * Core client for the Parsec service
 *
 * <p>The client exposes low-level functionality for using the Parsec service. Below you can see
 * code examples for a few of the operations supported.
 *
 * <p>Providers are abstracted representations of the secure elements that Parsec offers abstraction
 * over. Providers are the ones to execute the cryptographic operations requested by the user.
 *
 * <p>For all cryptographic operations an implicit provider is used which can be changed between
 * operations. The client starts with the default provider, the first one returned by the
 * ListProviders operation.
 *
 * <p>For crypto operations, if the implicit client provider is `ProviderId.CORE`, a client error of
 * `InvalidProvider` type is returned. See the operation-specific response codes returned by the
 * service in the operation's page
 * [here](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/index.html).
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
@Slf4j
@SuppressWarnings("unused")
public class BasicClient {
  private OperationClient operationClient;
  private Authentication authData;
  private ProviderId implicitProvider;
  /**
   * Create a new Parsec client.
   *
   * <p>The client will be initialised with default values obtained from the service for the
   * implicit provider and for application identity.
   *
   * <p>* `app_name` is the application name to be used if direct authentication is the default
   * authentication choice
   *
   * <p>This client will use the default configuration. That includes using a Protobuf converter for
   * message bodies and a Unix Domain Socket IPC handler. The default timeout length is 60 seconds.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); # Ok(())} ```
   */
  public static BasicClient client(String appName) {
    return client(appName, null);
  }

  public static BasicClient client(String appName, IpcHandler ipcHandler) {
    BasicClient client =
        new BasicClient(OperationClient.withDefaults(), new Authentication.None(), ProviderId.CORE);
    if (ipcHandler != null) {
      client.setIpcHandler(ipcHandler);
    }
    client.setDefaultProvider();
    client.setDefaultAuth(appName);
    log.debug(
        "Parsec BasicClient created with implicit provider \"{}\" and authentication data \"{}\"",
        client.implicitProvider(),
        client.authData());
    return client;
  }

  /**
   * Create a client that can initially only be used with Core operations not necessitating
   * authentication (eg ping).
   *
   * <p>Setting an authentication method and an implicit provider is needed before calling crypto
   * operations.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; let client = BasicClient::new_naked(); let (major, minor) =
   * client.ping(); # Ok(())} ```
   */
  public static BasicClient naked() {
    return new BasicClient(
        OperationClient.withDefaults(), new Authentication.None(), ProviderId.CORE);
  }
  /**
   * Query the service for the list of authenticators provided and use the first one as default
   *
   * <p>* `app_name` is to be used if direct authentication is the default choice
   *
   * <p># Errors
   *
   * <p>If no authenticator is reported by the service, a `NoAuthenticator` error kind is returned.
   *
   * <p>If the default authenticator is `DirectAuthenticator` and `app_name` was set to `None`, an
   * error of type `MissingParam` is returned.
   *
   * <p>If none of the authenticators returned by the service is supported, `NoAuthenticator` is
   * returned.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use parsec_client::core::interface::requests::ProviderId; let mut
   * client = BasicClient::new_naked(); Set the default authenticator but choose a specific
   * provider. client.set_implicitProvider(ProviderId::Pkcs11);
   * client.set_default_auth(Some("main_client".to_string())); # Ok(())} ```
   */
  public void setDefaultAuth(String appName) {
    NativeResult.ListAuthenticatorsResult authenticators = this.listAuthenticators();
    if (authenticators.getAuthenticators().isEmpty()) {
      throw new NoAuthenticatorException(null);
    }
    for (NativeResult.ListAuthenticatorsResult.AuthenticatorInfo authenticator :
        authenticators.getAuthenticators()) {
      switch (authenticator.getId()) {
        case DIRECT:
          this.authData =
              ofNullable(appName)
                  .map(Authentication.Direct::new)
                  .orElseThrow(MissingParamException::new);
          break;
        case UNIX_PEER_CREDENTIALS:
          this.authData = new Authentication.UnixPeerCredentials();
          break;

        case JWT_SVID:
          this.authData = new Authentication.JwtSvid();
          break;
        default:
          log.warn(
              "Authenticator of type \"{}\" not supported by this client library",
              authenticator.getId());
          continue;
      }
      return;
    }
    throw new NoAuthenticatorException(null);
  }
  /**
   * Update the authentication data of the client.
   *
   * <p>This is useful if you want to use a different authentication method than the default one.
   *
   * <p># Example
   *
   * <p>See [`set_default_provider`].
   */
  public void setAuthData(Authentication authData) {
    this.authData = authData;
  }

  /**
   * Retrieve authentication data of the client.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use parsec_client::auth::Authentication; let mut client =
   * BasicClient::new_naked(); client.set_authData(Authentication::UnixPeerCredentials);
   * assert_eq!(Authentication::UnixPeerCredentials, client.authData()); # Ok(())} ```
   */
  public Authentication authData() {
    return this.authData;
  }
  /**
   * Query for the service provider list and set the default one as implicit
   *
   * <p># Errors
   *
   * <p>If no provider is returned by the service, an client error of `NoProvider` type is returned.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use parsec_client::auth::Authentication; let mut client =
   * BasicClient::new_naked(); Use the default provider but use a specific authentication.
   * client.set_default_provider(); client.set_authData(Authentication::UnixPeerCredentials); #
   * Ok(())} ```
   */
  public void setDefaultProvider() {
    NativeResult.ListProvidersResult providers = this.listProviders();
    if (providers.getProviders().isEmpty()) {
      throw new NoProviderException();
    }
    this.implicitProvider = providers.getProviders().get(0).getId();
  }
  /**
   * Set the provider that the client will be implicitly working with.
   *
   * <p># Example
   *
   * <p>See [`set_default_auth`].
   */
  public void setImplicitProvider(ProviderId provider) {
    this.implicitProvider = provider;
  }

  /**
   * Retrieve client's implicit provider.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use parsec_client::core::interface::requests::ProviderId; let mut
   * client = BasicClient::new_naked(); client.set_implicitProvider(ProviderId::Pkcs11);
   * assert_eq!(ProviderId::Pkcs11, client.implicitProvider()); # Ok(())} ```
   */
  public ProviderId implicitProvider() {
    return this.implicitProvider;
  }

  /**
   * **[Core Operation]** List the opcodes supported by the specified provider.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { # use
   * std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use parsec_client::core::interface::requests::{Opcode, ProviderId};
   *
   * <p>let client: BasicClient = BasicClient::new(None); let opcodes =
   * client.list_opcodes(ProviderId::Pkcs11); if opcodes.contains(&Opcode::PsaGenerateRandom) { let
   * random_bytes = client.psa_generate_random(10); } # Ok(())} # Ok(())} ```
   */
  public NativeResult.ListOpcodesResult listOpcodes(ProviderId provider) {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.ListOpcodesOperation.builder().providerId(provider).build(),
            ProviderId.CORE,
            this.authData);
    if (res instanceof NativeResult.ListOpcodesResult) {
      return (NativeResult.ListOpcodesResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * **[Core Operation]** List the providers that are supported by the service.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let mut client: BasicClient = BasicClient::new_naked(); let providers =
   * client.list_providers(); Set the second most prioritary provider
   * client.set_implicitProvider(providers[1].id); # Ok(())} ```
   */
  public NativeResult.ListProvidersResult listProviders() {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.ListProvidersOperation.builder().build(),
            ProviderId.CORE,
            this.authData);
    if (res instanceof NativeResult.ListProvidersResult) {
      return (NativeResult.ListProvidersResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * **[Core Operation]** List the authenticators that are supported by the service.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let opcodes =
   * client.list_authenticators(); # Ok(())} ```
   */
  public NativeResult.ListAuthenticatorsResult listAuthenticators() {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.ListAuthenticatorsOperation.builder().build(),
            ProviderId.CORE,
            this.authData);
    if (res instanceof NativeResult.ListAuthenticatorsResult) {
      return (NativeResult.ListAuthenticatorsResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * **[Core Operation]** List all keys belonging to the application.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let keys = client.list_keys(); # Ok(())}
   * ```
   */
  public NativeResult.ListKeysResult listKeys() {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.ListKeysOperation.builder().build(), ProviderId.CORE, this.authData);
    if (res instanceof NativeResult.ListKeysResult) {
      return (NativeResult.ListKeysResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * Get the key attributes.
   *
   * <p>This is a convenience method that uses `list_keys` underneath.
   *
   * <p># Errors
   *
   * <p>Returns `NotFound` if a key with this name does not exist.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let attributes =
   * client.key_attributes("my_key"); # Ok(())} ```
   */
  public PsaKeyAttributes.KeyAttributes keyAttributes(String keyName) {
    return listKeys().getKeys().stream()
        .filter(ki -> ki.getName().equals(keyName))
        .findFirst()
        .orElseThrow(NotFoundException::new)
        .getAttributes();
  }
  /**
   * **[Core Operation, Admin Operation]** Lists all clients currently having data in the service.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let clients = client.list_clients(); #
   * Ok(())} ```
   */
  public NativeResult.ListClientsResult listClients() {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.ListClientsOperation.builder().build(), ProviderId.CORE, this.authData);
    if (res instanceof NativeResult.ListClientsResult) {
      return (NativeResult.ListClientsResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * **[Core Operation, Admin Operation]** Delete all data a client has in the service.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); client.delete_client("main_client"); #
   * Ok(())} ```
   */
  public void deleteClient(String client) {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.DeleteClientOperation.builder().client(client).build(),
            ProviderId.CORE,
            this.authData);
    if (!(res instanceof NativeResult.DeleteClientResult)) {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * **[Core Operation]** Send a ping request to the service.
   *
   * <p>This operation is intended for testing connectivity to the service and for retrieving the
   * maximum wire protocol version it supports.
   *
   * <p># Example
   *
   * <p>See [`new_naked`].
   */
  public NativeResult.PingResult ping() {
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PingOperation.builder().build(),
            ProviderId.CORE,
            new Authentication.None());

    if (res instanceof NativeResult.PingResult) {
      return (NativeResult.PingResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * **[Cryptographic Operation]** Generate a key.
   *
   * <p>Creates a new key with the given name within the namespace of the implicit client provider.
   * Any UTF-8 string is considered a valid key name, however names must be unique per provider.
   *
   * <p>Persistence of keys is implemented at provider level, and currently all providers persist
   * all the keys users create.
   *
   * <p>If this method returns an error, no key will have been generated and the name used will
   * still be available for another key.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); let key_attrs = Attributes { lifetime:
   * Lifetime::Persistent, key_type: Type::RsaKeyPair, bits: 2048, policy: Policy { usage_flags:
   * UsageFlags::default(), permitted_algorithms: AsymmetricSignature::RsaPkcs1v15Sign { hash_alg:
   * Hash::Sha256.into(), }.into(), }, }; client.psa_generate_key("my_key", key_attrs); # Ok(())}
   * ```
   */
  public void psaGenerateKey(String keyName, PsaKeyAttributes keyAttributes) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaGenerateKeyOperation.builder()
                .keyName(keyName)
                .attributes(keyAttributes)
                .build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaGenerateKeyResult) {
      return;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * **[Cryptographic Operation]** Destroy a key.
   *
   * <p>Given that keys are namespaced at a provider level, it is important to call
   * `psa_destroy_key` on the correct combination of implicit client provider and `keyName`.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); client.psa_destroy_key("my_key"); #
   * Ok(())} ```
   */
  public void psaDestroyKey(String keyName) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaDestroyKeyOperation.builder().keyName(keyName).build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaDestroyKeyResult) {
      return;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * **[Cryptographic Operation]** Import a key.
   *
   * <p>Creates a new key with the given name within the namespace of the implicit client provider
   * using the user-provided data. Any UTF-8 string is considered a valid key name, however names
   * must be unique per provider.
   *
   * <p>The key material should follow the appropriate binary format expressed
   * [here](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/psa_export_public_key.html).
   * Several crates (e.g. [`picky-asn1`](https://crates.io/crates/picky-asn1)) can greatly help in
   * dealing with binary encodings.
   *
   * <p>If this method returns an error, no key will have been imported and the name used will still
   * be available for another key.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags, EccFamily}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); let ecc_private_key = vec![ 0x26, 0xc8,
   * 0x82, 0x9e, 0x22, 0xe3, 0x0c, 0xa6, 0x3d, 0x29, 0xf5, 0xf7, 0x27, 0x39, 0x58, 0x47, 0x41, 0x81,
   * 0xf6, 0x57, 0x4f, 0xdb, 0xcb, 0x4d, 0xbb, 0xdd, 0x52, 0xff, 0x3a, 0xc0, 0xf6, 0x0d, ]; let
   * key_attrs = Attributes { lifetime: Lifetime::Persistent, key_type: Type::EccKeyPair {
   * curve_family: EccFamily::SecpR1, }, bits: 256, policy: Policy { usage_flags:
   * UsageFlags::default(), permitted_algorithms: AsymmetricSignature::RsaPkcs1v15Sign { hash_alg:
   * Hash::Sha256.into(), }.into(), }, }; client.psa_import_key("my_key", &ecc_private_key,
   * key_attrs); # Ok(())} ```
   */
  public void psaImportKey(String keyName, byte[] keyMaterial, PsaKeyAttributes.KeyAttributes keyAttributes) {
    ProviderId cryptoProvider = this.canProvideCrypto();
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaImportKeyOperation.builder()
                .keyName(keyName)
                .attributes(keyAttributes)
                .data(keyMaterial)
                .build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaImportKeyResult) {
      return;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * **[Cryptographic Operation]** Export a public key or the public part of a key pair.
   *
   * <p>The returned key material will follow the appropriate binary format expressed
   * [here](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/psa_export_public_key.html).
   * Several crates (e.g. [`picky-asn1`](https://crates.io/crates/picky-asn1)) can greatly help in
   * dealing with binary encodings.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let public_key_data =
   * client.psa_export_public_key("my_key"); # Ok(())} ```
   */
  public NativeResult.PsaExportPublicKeyResult psaExportPublicKey(String keyName) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaExportPublicKeyOperation.builder().keyName(keyName).build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaExportPublicKeyResult) {
      return (NativeResult.PsaExportPublicKeyResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * **[Cryptographic Operation]** Export a key.
   *
   * <p>The returned key material will follow the appropriate binary format expressed
   * [here](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/psa_export_key.html).
   * Several crates (e.g. [`picky-asn1`](https://crates.io/crates/picky-asn1)) can greatly help in
   * dealing with binary encodings.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient;
   *
   * <p>let client: BasicClient = BasicClient::new(None); let key_data =
   * client.psa_export_key("my_key"); # Ok(())} ```
   */
  public NativeResult.PsaExportKeyResult psaExportKey(String keyName) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaExportKeyOperation.builder().keyName(keyName).build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaExportKeyResult) {
      return (NativeResult.PsaExportKeyResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * **[Cryptographic Operation]** Create an asymmetric signature on a pre-computed message digest.
   *
   * <p>The key intended for signing **must** have its `sign_hash` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The signature will be created with the algorithm defined in `signAlgorithm`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p>`hash` must be a hash pre-computed over the message of interest with the algorithm specified
   * within `signAlgorithm`.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); Hash of a message pre-calculated with
   * SHA-256. let hash = vec![ 0x69, 0x3E, 0xDB, 0x1B, 0x22, 0x79, 0x03, 0xF4, 0xC0, 0xBF, 0xD6,
   * 0x91, 0x76, 0x37, 0x84, 0xA2, 0x94, 0x8E, 0x92, 0x50, 0x35, 0xC2, 0x8C, 0x5C, 0x3C, 0xCA, 0xFE,
   * 0x18, 0xE8, 0x81, 0x37, 0x78, ]; let signature = client.psa_sign_hash("my_key", &hash,
   * AsymmetricSignature::RsaPkcs1v15Sign { hash_alg: Hash::Sha256.into(), }); # Ok(())} ```
   */
  public NativeResult.PsaSignHashResult psaSignHash(
      String keyName, byte[] hash, PsaAlgorithm.Algorithm.AsymmetricSignature signAlgorithm) {

    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaSignHashOperation.builder()
                .keyName(keyName)
                .alg(signAlgorithm)
                .hash(hash)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaSignHashResult) {
      return (NativeResult.PsaSignHashResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * **[Cryptographic Operation]** Verify an existing asymmetric signature over a pre-computed
   * message digest.
   *
   * <p>The key intended for signing **must** have its `verify_hash` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The signature will be verifyied with the algorithm defined in `sign_algorithm`, but only
   * after checking that the key policy and type conform with it.
   *
   * <p>`hash` must be a hash pre-computed over the message of interest with the algorithm specified
   * within `sign_algorithm`.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); Hash of a message pre-calculated with
   * SHA-256. let hash = vec![ 0x69, 0x3E, 0xDB, 0x1B, 0x22, 0x79, 0x03, 0xF4, 0xC0, 0xBF, 0xD6,
   * 0x91, 0x76, 0x37, 0x84, 0xA2, 0x94, 0x8E, 0x92, 0x50, 0x35, 0xC2, 0x8C, 0x5C, 0x3C, 0xCA, 0xFE,
   * 0x18, 0xE8, 0x81, 0x37, 0x78, ]; let alg = AsymmetricSignature::RsaPkcs1v15Sign { hash_alg:
   * Hash::Sha256.into(), }; let signature = client.psa_sign_hash("my_key", &hash, alg);
   * client.psa_verify_hash("my_key", &hash, alg, &signature); # Ok(())} ```
   */
  public NativeResult.PsaVerifyHashResult psaVerifyHash(
      String keyName,
      byte[] hash,
      PsaAlgorithm.Algorithm.AsymmetricSignature signAlgorithm,
      byte[] signature) {

    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaVerifyHashOperation.builder()
                .keyName(keyName)
                .alg(signAlgorithm)
                .hash(hash)
                .signature(signature)
                .build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaVerifyHashResult) {
      return (NativeResult.PsaVerifyHashResult) res;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * [Cryptographic Operation]** Create an asymmetric signature on a message.
   *
   * <p>The key intended for signing **must** have its `sign_message` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The signature will be created with the algorithm defined in `sign_algorithm`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); let message = "This is the message to sign
   * which can be of any size!".as_bytes(); let signature = client.psa_sign_message( "my_key",
   * message, AsymmetricSignature::RsaPkcs1v15Sign { hash_alg: Hash::Sha256.into(), } ); # Ok(())}
   * ```
   */
  public NativeResult.PsaSignMessageResult psaSignMessage(
      String keyName, byte[] message, PsaAlgorithm.Algorithm.AsymmetricSignature signAlgorithm) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaSignMessageOperation.builder()
                .keyName(keyName)
                .alg(signAlgorithm)
                .message(message)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaSignMessageResult) {
      return (NativeResult.PsaSignMessageResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Verify an existing asymmetric signature over a message.
   *
   * <p>The key intended for signing **must** have its `verify_message` flag set to `true` in its
   * [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The signature will be verifyied with the algorithm defined in `sign_algorithm`, but only
   * after checking that the key policy and type conform with it.
   *
   * <p># Example
   *
   * <p>```no_run # use std::error::Error; # # fn main() -> Result<(), Box<dyn Error>> { use
   * parsec_client::BasicClient; use
   * parsec_client::core::interface::operations::psa_key_attributes::{Attributes, Lifetime, Policy,
   * Type, UsageFlags}; use
   * parsec_client::core::interface::operations::psa_algorithm::{AsymmetricSignature, Hash};
   *
   * <p>let client: BasicClient = BasicClient::new(None); let message = "This is the message to sign
   * which can be of any size!".as_bytes(); let alg = AsymmetricSignature::RsaPkcs1v15Sign {
   * hash_alg: Hash::Sha256.into(), }; let signature = client.psa_sign_message("my_key", message,
   * alg); client.psa_verify_message("my_key", message, alg, &signature); # Ok(())} ```
   */
  public NativeResult psaVerifyMessage(
      String keyName,
      byte[] msg,
      PsaAlgorithm.Algorithm.AsymmetricSignature signAlgorithm,
      byte[] signature) {

    ProviderId cryptoProvider = this.canProvideCrypto();
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaVerifyMessageOperation.builder()
                .keyName(keyName)
                .alg(signAlgorithm)
                .message(msg)
                .signature(signature)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaVerifyMessageResult) {
      return res;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * [Cryptographic Operation]** Encrypt a short message.
   *
   * <p>The key intended for encrypting **must** have its `encrypt` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The encryption will be performed with the algorithm defined in `alg`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p>`salt` can be provided if supported by the algorithm. If the algorithm does not support
   * salt, pass an empty vector. If the algorithm supports optional salt, pass an empty vector to
   * indicate no salt. For RSA PKCS#1 v1.5 encryption, no salt is supported.
   */
  public NativeResult.PsaAsymmetricEncryptResult psaAsymmetricEncrypt(
      String keyName,
      PsaAlgorithm.Algorithm.AsymmetricEncryption encryptAlg,
      byte[] plaintext,
      byte[] salt) {

    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaAsymmetricEncryptOperation.builder()
                .keyName(keyName)
                .alg(encryptAlg)
                .plaintext(plaintext)
                .salt(salt)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaAsymmetricEncryptResult) {
      return (NativeResult.PsaAsymmetricEncryptResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Decrypt a short message.
   *
   * <p>The key intended for decrypting **must** have its `decrypt` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>`salt` can be provided if supported by the algorithm. If the algorithm does not support
   * salt, pass an empty vector. If the algorithm supports optional salt, pass an empty vector to
   * indicate no salt. For RSA PKCS#1 v1.5 encryption, no salt is supported.
   *
   * <p>The decryption will be performed with the algorithm defined in `alg`, but only after
   * checking that the key policy and type conform with it.
   */
  public NativeResult.PsaAeadDecryptResult psaAsymmetricDecrypt(
      String keyName,
      PsaAlgorithm.Algorithm.AsymmetricEncryption encryptAlg,
      byte[] ciphertext,
      byte[] salt) {

    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaAsymmetricDecryptOperation.builder()
                .keyName(keyName)
                .alg(encryptAlg)
                .ciphertext(ciphertext)
                .salt(salt)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaAeadDecryptResult) {
      return (NativeResult.PsaAeadDecryptResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }
  /**
   * [Cryptographic Operation]** Compute hash of a message.
   *
   * <p>The hash computation will be performed with the algorithm defined in `alg`.
   */
  public NativeResult.PsaHashComputeResult psaHashCompute(
      PsaAlgorithm.Algorithm.Hash alg, byte[] input) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaHashComputeOperation.builder().alg(alg).input(input).build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaHashComputeResult) {
      return (NativeResult.PsaHashComputeResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Compute hash of a message and compare it with a reference value.
   *
   * <p>The hash computation will be performed with the algorithm defined in `alg`.
   *
   * <p>If this operation returns no error, the hash was computed successfully and it matches the
   * reference value.
   */
  public NativeResult.PsaHashCompareResult psaHashCompare(
      PsaAlgorithm.Algorithm.Hash alg, byte[] input, byte[] hash) {
    ProviderId cryptoProvider = this.canProvideCrypto();
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaHashCompareOperation.builder()
                .alg(alg)
                .input(input)
                .hash(hash)
                .build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaHashCompareResult) {
      return (NativeResult.PsaHashCompareResult) res;
    }
    throw new InvalidServiceResponseTypeException();
  }

  /**
   * [Cryptographic Operation]** Authenticate and encrypt a short message.
   *
   * <p>The key intended for decrypting **must** have its `encrypt` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The encryption will be performed with the algorithm defined in `alg`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p>`nonce` must be appropriate for the selected `alg`.
   *
   * <p>For algorithms where the encrypted data and the authentication tag are defined as separate
   * outputs, the returned buffer will contain the encrypted data followed by the authentication
   * data.
   */
  public NativeResult.PsaAeadEncryptResult psaAeadEncrypt(
      String keyName,
      PsaAlgorithm.Algorithm.Aead encryptAlg,
      byte[] nonce,
      byte[] additionalData,
      byte[] plaintext) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaAeadEncryptOperation.builder()
                .keyName(keyName)
                .alg(encryptAlg)
                .nonce(nonce)
                .additionalData(additionalData)
                .plaintext(plaintext)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaAeadEncryptResult) {
      return (NativeResult.PsaAeadEncryptResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Decrypt and authenticate a short message.
   *
   * <p>The key intended for decrypting **must** have its `decrypt` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The decryption will be performed with the algorithm defined in `alg`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p>`nonce` must be appropriate for the selected `alg`.
   *
   * <p>For algorithms where the encrypted data and the authentication tag are defined as separate
   * inputs, `ciphertext` must contain the encrypted data followed by the authentication data.
   */
  public NativeResult.PsaAeadDecryptResult psaAeadDecrypt(
      String keyName,
      PsaAlgorithm.Algorithm.Aead encryptAlg,
      byte[] nonce,
      byte[] additionalData,
      byte[] ciphertext) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaAeadDecryptOperation.builder()
                .keyName(keyName)
                .alg(encryptAlg)
                .nonce(nonce)
                .additionalData(additionalData)
                .ciphertext(ciphertext)
                .build(),
            cryptoProvider,
            this.authData);

    if (res instanceof NativeResult.PsaAeadDecryptResult) {
      return (NativeResult.PsaAeadDecryptResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Perform a raw key agreement.
   *
   * <p>The provided private key **must** have its `derive` flag set to `true` in its [key
   * policy](https://docs.rs/parsec-interface//parsec_interface/operations/psa_key_attributes/struct.Policy.html).
   *
   * <p>The raw_key_agreement will be performed with the algorithm defined in `alg`, but only after
   * checking that the key policy and type conform with it.
   *
   * <p>`peer_key` must be the peer public key to use in the raw key derivation. It must be in a
   * format supported by
   * [`PsaImportKey`](https://parallaxsecond.github.io/parsec-book/parsec_client/operations/psa_import_key.html).
   */
  public NativeResult.PsaRawKeyAgreementResult psaRawKeyAgreement(
      PsaRawKeyAgreement alg, String privateKeyName, byte[] peerKey) {
    ProviderId cryptoProvider = this.canProvideCrypto();
    NativeResult res =
        this.operationClient.processOperation(
            NativeOperation.PsaRawKeyAgreementOperation.builder()
                .alg(alg)
                .peerKey(peerKey)
                .privateKeyName(privateKeyName)
                .build(),
            cryptoProvider,
            this.authData);
    if (res instanceof NativeResult.PsaRawKeyAgreementResult) {
      return (NativeResult.PsaRawKeyAgreementResult) res;
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  /**
   * [Cryptographic Operation]** Generate some random bytes.
   *
   * <p>Generates a sequence of random bytes and returns them to the user.
   *
   * <p>If this method returns an error, no bytes will have been generated.
   *
   * <p># Example
   *
   * <p>See [`list_opcodes`].
   */
  public byte[] psaGenerateRandom(long nbytes) {
    ProviderId cryptoProvider = this.canProvideCrypto();

    NativeOperation.PsaGenerateRandomOperation op =
        NativeOperation.PsaGenerateRandomOperation.builder().size(nbytes).build();

    NativeResult res = this.operationClient.processOperation(op, cryptoProvider, this.authData);

    if (res instanceof NativeResult.PsaGenerateRandomResult) {
      return ((NativeResult.PsaGenerateRandomResult) res).getRandomBytes();
    } else {
      // Should really not be reached given the checks we do, but it's not impossible if some
      // changes happen in the interface
      throw new InvalidServiceResponseTypeException();
    }
  }

  ProviderId canProvideCrypto() {
    switch (this.implicitProvider) {
      case CORE:
        throw new InvalidProviderException(null);
      case MBED_CRYPTO:
      case PKCS11:
      case TPM:
      case TRUSTED_SERVICE:
      case CRYPTO_AUTH_LIB:
      default:
        return this.implicitProvider;
    }
  }

  void setMaxBodySize(long maxBodySize) {
    this.operationClient.setMaxBodySize(maxBodySize);
  }

  void setIpcHandler(IpcHandler ipcHandler) {
    this.operationClient.setIpcHandler(ipcHandler);
  }

  void setTimeout(Duration timeout) {
    this.operationClient.setTimeout(timeout);
  }
}
