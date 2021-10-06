package org.parallaxsecond.operations_protobuf;

import org.parallaxsecond.exceptions.InterfaceException;
import org.parallaxsecond.operations.Convert;
import org.parallaxsecond.operations.NativeResult;
import org.parallaxsecond.requests.BodyType;
import org.parallaxsecond.requests.Opcode;
import org.parallaxsecond.requests.ResponseStatus;
import org.parallaxsecond.requests.response.ResponseBody;
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
