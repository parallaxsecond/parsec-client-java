package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;

public class ListKeysProtobufOpConverter implements ProtobufOpConverter {
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
