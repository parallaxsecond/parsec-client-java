package org.parallaxsecond.parsec.protocol.operations_protobuf;

import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.ProviderId;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.list_keys.ListKeys;

import java.util.stream.Collectors;

public class ListKeysProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
    return NativeOperation.ListKeysOperation.builder().build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    return new RequestBody(ListKeys.Operation.newBuilder().build().toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    NativeResult.ListKeysResult listKeysResult = (NativeResult.ListKeysResult) result;

    return new ResponseBody(
        ListKeys.Result.newBuilder()
            .addAllKeys(
                listKeysResult.getKeys().stream()
                    .map(
                        ki ->
                            ListKeys.KeyInfo.newBuilder()
                                .setAttributes(ki.getAttributes())
                                .setProviderId(ki.getProviderId().getId())
                                .setName(ki.getName())
                                .build())
                    .collect(Collectors.toList()))
            .build()
            .toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    ListKeys.Result listKeysResult = ListKeys.Result.parseFrom(body.getBuffer());

    return NativeResult.ListKeysResult.builder()
        .keys(
            listKeysResult.getKeysList().stream()
                .map(
                    k ->
                        NativeResult.ListKeysResult.KeyInfo.builder()
                            .providerId(ProviderId.fromCode((byte) k.getProviderId()))
                            .attributes(k.getAttributes())
                            .name(k.getName())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
