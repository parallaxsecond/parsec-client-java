package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;
import ping.Ping;

public class PingProtobufOpConverter implements ProtobufOpConverter {

  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
    return NativeOperation.PingOperation.builder().build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    return new RequestBody(Ping.Operation.newBuilder().build().toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    Ping.Result ping = Ping.Result.parseFrom(body.getBuffer());
    return NativeResult.PingResult.builder()
        .wireProtocolVersionMaj((byte) ping.getWireProtocolVersionMaj())
        .wireProtocolVersionMin((byte) ping.getWireProtocolVersionMin())
        .build();
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    NativeResult.PingResult ping = (NativeResult.PingResult) result;
    return new ResponseBody(
        Ping.Result.newBuilder()
            .setWireProtocolVersionMaj(ping.getWireProtocolVersionMaj())
            .setWireProtocolVersionMin(ping.getWireProtocolVersionMin())
            .build()
            .toByteArray());
  }
}
