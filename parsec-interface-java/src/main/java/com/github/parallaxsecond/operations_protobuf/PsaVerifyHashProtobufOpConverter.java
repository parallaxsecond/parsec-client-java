package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeOperation.PsaVerifyHashOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import psa_verify_hash.PsaVerifyHash;

public class PsaVerifyHashProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaVerifyHash.Operation protoBufOp = PsaVerifyHash.Operation.parseFrom(body.getBuffer());
    return PsaVerifyHashOperation.builder()
        .hash(protoBufOp.getHash().toByteArray())
        .signature(protoBufOp.getSignature().toByteArray())
        .alg(protoBufOp.getAlg())
        .keyName(protoBufOp.getKeyName())
        .build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    PsaVerifyHashOperation verifyHashOperation = (PsaVerifyHashOperation) operation;
    return new RequestBody(
        PsaVerifyHash.Operation.newBuilder()
            .setAlg(verifyHashOperation.getAlg())
            .setHash(ByteString.copyFrom(verifyHashOperation.getHash()))
            .setSignature(ByteString.copyFrom(verifyHashOperation.getSignature()))
            .setKeyName(verifyHashOperation.getKeyName())
            .build()
            .toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    return new ResponseBody(PsaVerifyHash.Result.newBuilder().build().toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaVerifyHash.Result.parseFrom(body.getBuffer());
    return NativeResult.PsaVerifyHashResult.builder().build();
  }
}
