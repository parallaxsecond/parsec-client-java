package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.operations.NativeResult.ListKeysResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.ProviderId;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;
import list_keys.ListKeys;

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
    ListKeysResult listKeysResult = (ListKeysResult) result;

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
                        ListKeysResult.KeyInfo.builder()
                            .providerId(ProviderId.fromCode((byte) k.getProviderId()))
                            .attributes(k.getAttributes())
                            .name(k.getName())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
