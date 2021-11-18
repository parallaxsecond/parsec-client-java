package org.parallaxsecond.parsec.protocol.operations_protobuf;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.parallaxsecond.parsec.protobuf.psa_import_key.PsaImportKey;
import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.response.ResponseBody;

public class PsaImportKeyProtobufOpConverter implements ProtobufOpConverter {
    @Override
    public NativeOperation bodyToOperation(RequestBody body, Opcode opcode)
            throws InvalidProtocolBufferException {
        PsaImportKey.Operation protoBufOp = PsaImportKey.Operation.parseFrom(body.getBuffer());

        return NativeOperation.PsaImportKeyOperation.builder()
                .keyName(protoBufOp.getKeyName())
                .data(protoBufOp.getData().toByteArray())
                .attributes(protoBufOp.getAttributes())
                .build();
    }

    @Override
    public RequestBody operationToBody(NativeOperation operation) {
        NativeOperation.PsaImportKeyOperation psaImportKeyOperation =
                (NativeOperation.PsaImportKeyOperation) operation;
        return new RequestBody(
                PsaImportKey.Operation.newBuilder()
                        .setKeyName(psaImportKeyOperation.getKeyName())
                        .setData(ByteString.copyFrom(psaImportKeyOperation.getData()))
                        .setAttributes(psaImportKeyOperation.getAttributes())
                        .build()
                        .toByteArray());
    }

    @Override
    public ResponseBody resultToBody(NativeResult result) {
        return new ResponseBody(PsaImportKey.Result.newBuilder().build().toByteArray());
    }

    @Override
    public NativeResult tryBodyToResult(ResponseBody body, Opcode opcode)
            throws InvalidProtocolBufferException {
        return NativeResult.PsaImportKeyResult.builder().build();
    }
}
