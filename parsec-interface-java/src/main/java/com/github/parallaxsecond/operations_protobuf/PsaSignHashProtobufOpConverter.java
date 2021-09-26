package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeOperation.PsaSignHashOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.operations.NativeResult.PsaSignHashResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import psa_sign_hash.PsaSignHash;

public class PsaSignHashProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaSignHash.Operation protoBufOp = PsaSignHash.Operation.parseFrom(body.getBuffer());
    return PsaSignHashOperation.builder()
        .keyName(protoBufOp.getKeyName())
        .alg(protoBufOp.getAlg())
        .hash(protoBufOp.getHash().toByteArray())
        .build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    PsaSignHashOperation signHashOperation = (PsaSignHashOperation) operation;
    return new RequestBody(
        PsaSignHash.Operation.newBuilder()
            .setKeyName(signHashOperation.getKeyName())
            .setAlg(signHashOperation.getAlg())
            .setHash(ByteString.copyFrom(signHashOperation.getHash()))
            .build()
            .toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    PsaSignHashResult psaSignResult = (PsaSignHashResult) result;

    return new ResponseBody(
        PsaSignHash.Result.newBuilder()
            .setSignature(ByteString.copyFrom(psaSignResult.getSignature()))
            .build()
            .toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaSignHash.Result signHashResult = PsaSignHash.Result.parseFrom(body.getBuffer());
    return NativeResult.PsaSignHashResult.builder()
        .signature(signHashResult.getSignature().toByteArray())
        .build();
  }
}
