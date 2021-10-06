package org.parallaxsecond.operations_protobuf;

import org.parallaxsecond.operations.NativeOperation;
import org.parallaxsecond.operations.NativeResult;
import org.parallaxsecond.requests.Opcode;
import org.parallaxsecond.requests.request.RequestBody;
import org.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;

public class PsaSignMessageProtobufOpConverter implements ProtobufOpConverter {
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
