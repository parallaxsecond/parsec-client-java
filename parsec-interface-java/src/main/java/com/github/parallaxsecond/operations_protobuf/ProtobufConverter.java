package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.Convert;
import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.BodyType;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;

import java.util.HashMap;
import java.util.Map;

import static com.github.parallaxsecond.requests.Opcode.*;

public class ProtobufConverter implements Convert {
  private static final Map<Opcode, ProtobufOpConverter> converters = initConverters();

  @Override
  public BodyType bodyType() {
    return BodyType.PROTOBUF;
  }

  private static Map<Opcode, ProtobufOpConverter> initConverters() {
    Map<Opcode, ProtobufOpConverter> converters = new HashMap<>();
    converters.put(PING, new PingProtobufOpConverter());
    converters.put(PSA_GENERATE_KEY, new PsaGenerateKeyProtobufOpConverter());
    converters.put(PSA_DESTROY_KEY, new PsaDestroyKeyProtobufOpConverter());
    converters.put(PSA_SIGN_HASH, new PsaSignHashProtobufOpConverter());
    converters.put(PSA_VERIFY_HASH, new PsaVerifyHashProtobufOpConverter());
    converters.put(PSA_IMPORT_KEY, new PsaImportKeyProtobufOpConverter());
    converters.put(PSA_EXPORT_PUBLIC_KEY, new PsaExportPublicKeyProtobufOpConverter());
    converters.put(LIST_PROVIDERS, new ListProvidersProtobufOpConverter());
    converters.put(LIST_OPCODES, new ListOpcodesProtobufOpConverter());
    converters.put(PSA_ASYMMETRIC_ENCRYPT, new PsaAsymetricEncryptProtobufOpConverter());
    converters.put(PSA_ASYMMETRIC_DECRYPT, new PsaAsymmetricDecryptProtobufOpConverter());
    converters.put(PSA_EXPORT_KEY, new PsaExportKeyProtobufOpConverter());
    converters.put(PSA_GENERATE_RANDOM, new PsaGenerateRandomProtobufOpConverter());
    converters.put(LIST_AUTHENTICATORS, new ListAuthenticatorsProtobufOpConverter());
    converters.put(PSA_HASH_COMPUTE, new PsaHashComputeProtobufOpConverter());
    converters.put(PSA_HASH_COMPARE, new PsaHashCompareProtobufOpConverter());
    converters.put(PSA_AEAD_ENCRYPT, new PsaAeadEncryptProtobufOpConverter());
    converters.put(PSA_AEAD_DECRYPT, new PsaAeadDecryptProtobufOpConverter());
    converters.put(PSA_RAW_KEY_AGREEMENT, new PsaRawKeyAgreementProtobufOpConverter());
    converters.put(PSA_SIGN_MESSAGE, new PsaSignMessageProtobufOpConverter());
    converters.put(PSA_VERIFY_MESSAGE, new PsaVerifyMessageProtobufOpConverter());
    converters.put(LIST_KEYS, new ListKeysProtobufOpConverter());
    converters.put(LIST_CLIENTS, new ListClientsProtobufOpConverter());
    converters.put(DELETE_CLIENT, new DeleteClientProtobufOpConverter());
    return converters;
  }

  private ProtobufOpConverter getConverter(Opcode opcode) {
    ProtobufOpConverter converter = converters.get(opcode);
    if (converter == null) {
      throw new IllegalStateException("not implemented " + opcode);
    }
    return converter;
  }

  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
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
