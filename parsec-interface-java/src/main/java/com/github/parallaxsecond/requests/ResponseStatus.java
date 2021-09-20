package com.github.parallaxsecond.requests;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * C-like enum mapping response status options to their code.
 *
 * <p>See the [status
 * code](https://parallaxsecond.github.io/parsec-book/parsec_client/status_codes.html) page for a
 * broader description of these codes.
 */
@RequiredArgsConstructor
// FIXME enum naming convention all caps _
@Getter
public enum ResponseStatus {
  /** Successful operation */
  Success(0, "successful operation"),
  /** Requested provider ID does not match that of the backend */
  WrongProviderId(1, "requested provider ID does not match that of the backend"),
  /** Requested content type is not supported by the backend */
  ContentTypeNotSupported(2, "requested content type is not supported by the backend"),

  /** Requested accept type is not supported by the backend */
  AcceptTypeNotSupported(3, "requested accept type is not supported by the backend"),

  /** Requested version is not supported by the backend */
  WireProtocolVersionNotSupported(4, "requested version is not supported by the backend"),
  /** No provider registered for the requested provider ID */
  ProviderNotRegistered(5, "no provider registered for the requested provider ID"),

  /** No provider defined for requested provider ID */
  ProviderDoesNotExist(6, "no provider defined for requested provider ID"),
  /** Failed to deserialize the body of the message */
  DeserializingBodyFailed(7, "failed to deserialize the body of the message"),
  /** Failed to serialize the body of the message */
  SerializingBodyFailed(8, "failed to serialize the body of the message"),
  /** Requested operation is not defined */
  OpcodeDoesNotExist(9, "requested operation is not defined"),
  /** Response size exceeds allowed limits */
  ResponseTooLarge(10, "response size exceeds allowed limits"),
  /** Authentication failed */
  AuthenticationError(11, "authentication failed"),
  /** Authenticator not supported */
  AuthenticatorDoesNotExist(12, "authenticator not supported"),

  /** Authenticator not supported */
  AuthenticatorNotRegistered(13, "authenticator not supported"),
  /** Internal error in the Key Info Manager */
  KeyInfoManagerError(14, "internal error in the Key Info Manager"),
  /** Generic input/output error */
  ConnectionError(15, "generic input/output error"),
  /** Invalid value for this data type */
  InvalidEncoding(16, "invalid value for this data type"),
  /** Constant fields in header are invalid */
  InvalidHeader(17, "constant fields in header are invalid"),
  /** The UUID vector needs to only contain 16 bytes */
  WrongProviderUuid(18, "the UUID vector needs to only contain 16 bytes"),
  /** Request did not provide a required authentication */
  NotAuthenticated(19, "request did not provide a required authentication"),
  /** Request length specified in the header is above defined limit */
  BodySizeExceedsLimit(20, "request length specified in the header is above defined limit"),
  /** The operation requires admin privilege */
  AdminOperation(21, "the operation requires admin privilege"),
  /** An error occurred that does not correspond to any defined failure cause */
  PsaErrorGenericError(
      1132, "an error occurred that does not correspond to any defined failure cause"),
  /** The requested operation or a parameter is not supported by this implementation */
  PsaErrorNotSupported(
      1134, "the requested operation or a parameter is not supported by this implementation"),
  /** The requested action is denied by a policy */
  PsaErrorNotPermitted(1133, "the requested action is denied by a policy"),
  /** An output buffer is too small */
  PsaErrorBufferTooSmall(1138, "an output buffer is too small"),
  /** Asking for an item that already exists */
  PsaErrorAlreadyExists(1139, "asking for an item that already exists"),

  /** Asking for an item that doesn't exist */
  PsaErrorDoesNotExist(1140, "asking for an item that doesn't exist"),
  /** The requested action cannot be performed in the current state */
  PsaErrorBadState(1137, "the requested action cannot be performed in the current state"),
  /** The parameters passed to the function are invalid */
  PsaErrorInvalidArgument(1135, "the parameters passed to the function are invalid"),
  /** There is not enough runtime memory */
  PsaErrorInsufficientMemory(1141, "there is not enough runtime memory"),
  /** There is not enough persistent storage */
  PsaErrorInsufficientStorage(1142, "there is not enough persistent storage"),
  /** There was a communication failure inside the implementation */
  PsaErrorCommunicationFailure(1145, "there was a communication failure inside the implementation"),
  /** There was a storage failure that may have led to data loss */
  PsaErrorStorageFailure(1146, "there was a storage failure that may have led to data loss"),
  /** Stored data has been corrupted */
  PsaErrorDataCorrupt(1152, "stored data has been corrupted"),
  /** Data read from storage is not valid for the implementation */
  PsaErrorDataInvalid(1153, "data read from storage is not valid for the implementation"),

  /** A hardware failure was detected */
  PsaErrorHardwareFailure(1147, "a hardware failure was detected"),
  /** A tampering attempt was detected */
  PsaErrorCorruptionDetected(1151, "a tampering attempt was detected"),
  /** There is not enough entropy to generate random data needed for the requested action */
  PsaErrorInsufficientEntropy(
      1148, "there is not enough entropy to generate random data needed for the requested action"),
  /** The signature, MAC or hash is incorrect */
  PsaErrorInvalidSignature(1149, "the signature, MAC or hash is incorrect"),
  /** The decrypted padding is incorrect */
  PsaErrorInvalidPadding(1150, "the decrypted padding is incorrect"),
  /** Insufficient data when attempting to read from a resource */
  PsaErrorInsufficientData(1143, "insufficient data when attempting to read from a resource"),
  /** The key handle is not valid */
  PsaErrorInvalidHandle(1136, "the key handle is not valid");
  private final short id;
  private final String description;

  ResponseStatus(int id, String description) {
    this((short) id, description);
  }

  public static ResponseStatus fromCode(int code) {
    for (ResponseStatus status : values()) {
      if (status.getId() == code) {
        return status;
      }
    }
    throw new IllegalStateException("unknown responseStatus for code " + code);
  }
}
