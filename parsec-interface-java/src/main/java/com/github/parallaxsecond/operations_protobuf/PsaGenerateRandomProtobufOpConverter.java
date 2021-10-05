package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import psa_generate_random.PsaGenerateRandom;

import static com.github.parallaxsecond.operations.NativeOperation.PsaGenerateRandomOperation;
import static com.github.parallaxsecond.operations.NativeResult.PsaGenerateRandomResult;

public class PsaGenerateRandomProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) throws InvalidProtocolBufferException {
    PsaGenerateRandom.Operation protoBufOp = PsaGenerateRandom.Operation.parseFrom(body.getBuffer());
    return PsaGenerateRandomOperation.builder()
        .size(protoBufOp.getSize()).build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    PsaGenerateRandomOperation psaGenerateRandomOperation = (PsaGenerateRandomOperation) operation;
    return new RequestBody(
        PsaGenerateRandom.Operation.newBuilder()
            .setSize(psaGenerateRandomOperation.getSize())
            .build()
            .toByteArray()
    );
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    PsaGenerateRandomResult psaGenerateRandomResult = (PsaGenerateRandomResult) result;
    return new ResponseBody(
        PsaGenerateRandom.Result.newBuilder()
            .setRandomBytes(ByteString.copyFrom(psaGenerateRandomResult.getRandomBytes()))
            .build()
            .toByteArray()
    );

  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaGenerateRandom.Result psaGenerateRandomResult = PsaGenerateRandom.Result.parseFrom(body.getBuffer());
    return NativeResult.PsaGenerateRandomResult.builder()
        .randomBytes(psaGenerateRandomResult.getRandomBytes().toByteArray())
        .build();
  }
}
