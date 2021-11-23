package org.parallaxsecond.parsec.protocol.operations_protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.psa_generate_key.PsaGenerateKey;
import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;

public class PsaGenerateKeyProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaGenerateKey.Operation protoBufOp = PsaGenerateKey.Operation.parseFrom(body.getBuffer());

    return NativeOperation.PsaGenerateKeyOperation.builder()
        .keyName(protoBufOp.getKeyName())
        .attributes(protoBufOp.getAttributes())
        .build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    NativeOperation.PsaGenerateKeyOperation psaGenerateKeyOperation =
        (NativeOperation.PsaGenerateKeyOperation) operation;
    return new RequestBody(
        PsaGenerateKey.Operation.newBuilder()
            .setKeyName(psaGenerateKeyOperation.getKeyName())
            .setAttributes(psaGenerateKeyOperation.getAttributes())
            .build()
            .toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    return new ResponseBody(PsaGenerateKey.Result.newBuilder().build().toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    return NativeResult.PsaGenerateKeyResult.builder().build();
  }
}
