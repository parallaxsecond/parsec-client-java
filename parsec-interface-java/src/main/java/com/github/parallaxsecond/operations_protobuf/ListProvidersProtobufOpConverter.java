package com.github.parallaxsecond.operations_protobuf;

import com.github.parallaxsecond.operations.NativeOperation;
import com.github.parallaxsecond.operations.NativeResult;
import com.github.parallaxsecond.requests.Opcode;
import com.github.parallaxsecond.requests.ProviderId;
import com.github.parallaxsecond.requests.request.RequestBody;
import com.github.parallaxsecond.requests.response.ResponseBody;
import com.google.protobuf.InvalidProtocolBufferException;
import list_providers.ListProviders;

import java.util.UUID;
import java.util.stream.Collectors;

public class ListProvidersProtobufOpConverter implements ProtobufOpConverter {
  @Override
  public NativeOperation bodyToOperation(RequestBody body, Opcode opcode) {
    return NativeOperation.ListProvidersOperation.builder().build();
  }

  @Override
  public RequestBody operationToBody(NativeOperation operation) {
    return new RequestBody(ListProviders.Operation.newBuilder().build().toByteArray());
  }

  @Override
  public ResponseBody resultToBody(NativeResult result) {
    NativeResult.ListProvidersResult list = (NativeResult.ListProvidersResult) result;

    return new ResponseBody(
        ListProviders.Result.newBuilder()
            .addAllProviders(
                list.getProviders().stream()
                    .map(
                        p ->
                            ListProviders.ProviderInfo.newBuilder()
                                .setUuid(p.getUuid().toString())
                                .setDescription(p.getDescription())
                                .setVendor(p.getVendor())
                                .setVersionMaj(p.getVersionMaj())
                                .setVersionMin(p.getVersionMin())
                                .setVersionRev(p.getVersionRev())
                                .setId(p.getId().getId())
                                .build())
                    .collect(Collectors.toList()))
            .build()
            .toByteArray());
  }

  @Override
  public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
      throws InvalidProtocolBufferException {
    ListProviders.Result list = ListProviders.Result.parseFrom(body.getBuffer());
    return NativeResult.ListProvidersResult.builder()
        .providers(
            list.getProvidersList().stream()
                .map(
                    p ->
                        NativeResult.ListProvidersResult.ProviderInfo.builder()
                            .uuid(UUID.fromString(p.getUuid()))
                            .description(p.getDescription())
                            .vendor(p.getVendor())
                            .versionMaj(p.getVersionMaj())
                            .versionMin(p.getVersionMin())
                            .versionRev(p.getVersionRev())
                            .id(ProviderId.fromCode((byte) p.getId()))
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
