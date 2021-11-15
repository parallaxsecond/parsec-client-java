package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.Convert;
import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.BodyType;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;

import java.util.HashMap;
import java.util.Map;

public class ProtobufConverter implements Convert {
  private static final Map<Opcode, ProtobufOpConverter> converters = initConverters();

  private static Map<Opcode, ProtobufOpConverter> initConverters() {
    Map<Opcode, ProtobufOpConverter> converters = new HashMap<>();
    converters.put(Opcode.PING, new PingProtobufOpConverter());
    converters.put(Opcode.PSA_GENERATE_KEY, new PsaGenerateKeyProtobufOpConverter());
    converters.put(Opcode.PSA_DESTROY_KEY, new PsaDestroyKeyProtobufOpConverter());
    converters.put(Opcode.PSA_SIGN_HASH, new PsaSignHashProtobufOpConverter());
    converters.put(Opcode.PSA_VERIFY_HASH, new PsaVerifyHashProtobufOpConverter());
    converters.put(Opcode.PSA_IMPORT_KEY, new PsaImportKeyProtobufOpConverter());
    converters.put(Opcode.PSA_EXPORT_PUBLIC_KEY, new PsaExportPublicKeyProtobufOpConverter());
    converters.put(Opcode.LIST_PROVIDERS, new ListProvidersProtobufOpConverter());
    converters.put(Opcode.LIST_OPCODES, new ListOpcodesProtobufOpConverter());
    converters.put(Opcode.PSA_ASYMMETRIC_ENCRYPT, new PsaAsymetricEncryptProtobufOpConverter());
    converters.put(Opcode.PSA_ASYMMETRIC_DECRYPT, new PsaAsymmetricDecryptProtobufOpConverter());
    converters.put(Opcode.PSA_EXPORT_KEY, new PsaExportKeyProtobufOpConverter());
    converters.put(Opcode.PSA_GENERATE_RANDOM, new PsaGenerateRandomProtobufOpConverter());
    converters.put(Opcode.LIST_AUTHENTICATORS, new ListAuthenticatorsProtobufOpConverter());
    converters.put(Opcode.PSA_HASH_COMPUTE, new PsaHashComputeProtobufOpConverter());
    converters.put(Opcode.PSA_HASH_COMPARE, new PsaHashCompareProtobufOpConverter());
    converters.put(Opcode.PSA_AEAD_ENCRYPT, new PsaAeadEncryptProtobufOpConverter());
    converters.put(Opcode.PSA_AEAD_DECRYPT, new PsaAeadDecryptProtobufOpConverter());
    converters.put(Opcode.PSA_RAW_KEY_AGREEMENT, new PsaRawKeyAgreementProtobufOpConverter());
    converters.put(Opcode.PSA_SIGN_MESSAGE, new PsaSignMessageProtobufOpConverter());
    converters.put(Opcode.PSA_VERIFY_MESSAGE, new PsaVerifyMessageProtobufOpConverter());
    converters.put(Opcode.LIST_KEYS, new ListKeysProtobufOpConverter());
    converters.put(Opcode.LIST_CLIENTS, new ListClientsProtobufOpConverter());
    converters.put(Opcode.DELETE_CLIENT, new DeleteClientProtobufOpConverter());
    return converters;
  }

  @Override
  public BodyType bodyType() {
    return BodyType.PROTOBUF;
  }

  private ProtobufOpConverter getConverter(Opcode opcode) {
    ProtobufOpConverter converter = converters.get(opcode);
    if (converter == null) {
      throw new IllegalStateException("not implemented " + opcode);
    }
    return converter;
  }

  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) throws Exception {
    return getConverter(opcode).bodyToOperation(body, opcode);
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    return getConverter(operation.getOpcode()).operationToBody(operation);
  }

  @Override
  public NativeResult bodyToResult(ResponseBody body, Opcode opcode) {
    return getConverter(opcode).bodyToResult(body, opcode);
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    return getConverter(result.getOpcode()).resultToBody(result);
  }
}
