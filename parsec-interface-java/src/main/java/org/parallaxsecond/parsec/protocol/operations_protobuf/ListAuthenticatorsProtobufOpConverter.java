package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.AuthType;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.list_authenticators.ListAuthenticators;

import java.util.stream.Collectors;

public class ListAuthenticatorsProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
    return NativeOperation.ListAuthenticatorsOperation.builder().build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    return new RequestBody(ListAuthenticators.Operation.newBuilder().build().toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    NativeResult.ListAuthenticatorsResult list = (NativeResult.ListAuthenticatorsResult) result;
    return new ResponseBody(
        ListAuthenticators.Result.newBuilder()
            .addAllAuthenticators(
                list.getAuthenticators().stream()
                    .map(
                        a ->
                            ListAuthenticators.AuthenticatorInfo.newBuilder()
                                .setDescription(a.getDescription())
                                .setVersionMaj(a.getVersionMaj())
                                .setVersionMin(a.getVersionMin())
                                .setVersionRev(a.getVersionRev())
                                .setId(a.getId().getId())
                                .build())
                    .collect(Collectors.toList()))
            .build()
            .toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {

    ListAuthenticators.Result list = ListAuthenticators.Result.parseFrom(body.getBuffer());

    return NativeResult.ListAuthenticatorsResult.builder()
        .authenticators(
            list.getAuthenticatorsList().stream()
                .map(
                    a ->
                        NativeResult.ListAuthenticatorsResult.AuthenticatorInfo.builder()
                            .description(a.getDescription())
                            .versionMaj(a.getVersionMaj())
                            .versionMin(a.getVersionMin())
                            .versionRev(a.getVersionRev())
                            .id(AuthType.fromCode((byte) a.getId()))
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
