package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.requests.InterfaceException;
import org.parallaxsecond.parsec.protocol.operations.Convert;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.BodyType;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.ResponseStatus;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
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
