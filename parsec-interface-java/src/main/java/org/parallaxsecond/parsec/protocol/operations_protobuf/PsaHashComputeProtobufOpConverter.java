package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;

public class PsaHashComputeProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    throw new IllegalStateException("not implemented"); // FIXME
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    throw new IllegalStateException("not implemented"); // FIXME
  }
}
