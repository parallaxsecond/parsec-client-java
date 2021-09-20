package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.exceptions.InterfaceException;
import com.github.parallaxsecond.operations.Convert;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.BodyType;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.ResponseStatus;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;

public interface ProtobufOpConverter extends Convert {
  @Override
  default BodyType bodyType() {
    return BodyType.PROTOBUF;
  }

  default NativeResult bodyToResult(ResponseBody body, Opcode opcode) {
    try {
      return tryBodyToResult(body, opcode);
    } catch (InvalidProtocolBufferException e) {
      throw new InterfaceException(ResponseStatus.DeserializingBodyFailed, e);
    }
  }

  NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException;
}
