package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.psa_verify_hash.PsaVerifyHash;

public class PsaVerifyHashProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    PsaVerifyHash.Operation protoBufOp = PsaVerifyHash.Operation.parseFrom(body.getBuffer());
    return NativeOperation.PsaVerifyHashOperation.builder()
        .hash(protoBufOp.getHash().toByteArray())
        .signature(protoBufOp.getSignature().toByteArray())
        .alg(protoBufOp.getAlg())
        .keyName(protoBufOp.getKeyName())
        .build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    NativeOperation.PsaVerifyHashOperation verifyHashOperation = (NativeOperation.PsaVerifyHashOperation) operation;
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
