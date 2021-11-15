package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.psa_generate_random.PsaGenerateRandom;

public class PsaGenerateRandomProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) throws InvalidProtocolBufferException {
    PsaGenerateRandom.Operation protoBufOp = PsaGenerateRandom.Operation.parseFrom(body.getBuffer());
    return NativeOperation.PsaGenerateRandomOperation.builder()
        .size(protoBufOp.getSize()).build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    NativeOperation.PsaGenerateRandomOperation psaGenerateRandomOperation = (NativeOperation.PsaGenerateRandomOperation) operation;
    return new RequestBody(
        PsaGenerateRandom.Operation.newBuilder()
            .setSize(psaGenerateRandomOperation.getSize())
            .build()
            .toByteArray()
    );
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    NativeResult.PsaGenerateRandomResult psaGenerateRandomResult = (NativeResult.PsaGenerateRandomResult) result;
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
