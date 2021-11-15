package org.parallaxsecond.parsec.client.core;

import org.parallaxsecond.parsec.client.Authentication;
import org.parallaxsecond.parsec.client.core.ipc_handler.IpcHandler;
import org.parallaxsecond.parsec.client.exceptions.ClientException;
import org.parallaxsecond.parsec.protocol.requests.InterfaceException;
import org.parallaxsecond.parsec.client.exceptions.InvalidServiceResponseTypeException;
import org.parallaxsecond.parsec.client.exceptions.ServiceException;
import org.parallaxsecond.parsec.protocol.operations.Convert;
import org.parallaxsecond.parsec.protocol.operations.NativeOperation;
import org.parallaxsecond.parsec.protocol.operations.NativeResult;
import org.parallaxsecond.parsec.protocol.operations_protobuf.ProtobufConverter;
import org.parallaxsecond.parsec.protocol.requests.Opcode;
import org.parallaxsecond.parsec.protocol.requests.ProviderId;
import org.parallaxsecond.parsec.protocol.requests.ResponseStatus;
import org.parallaxsecond.parsec.protocol.requests.request.Request;
import org.parallaxsecond.parsec.protocol.requests.request.RequestBody;
import org.parallaxsecond.parsec.protocol.requests.request.RequestHeader;
import org.parallaxsecond.parsec.protocol.requests.response.Response;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.Duration;

@RequiredArgsConstructor
@Builder
public class OperationClient {
  /** Converter that manages request body conversions Defaults to a Protobuf converter */
  private final Convert contentConverter;
  /** Converter that manages response body conversions Defaults to a Protobuf converter */
  private final Convert acceptConverter;
  /** Client for request and response objects */
  private final RequestClient requestClient;

  public static OperationClient withDefaults() {
    return OperationClient.builder()
        .contentConverter(new ProtobufConverter())
        .acceptConverter(new ProtobufConverter())
        .requestClient(RequestClient.withDefaults())
        .build();
  }

  public NativeResult processOperation(
      NativeOperation operation, ProviderId providerId, Authentication auth) {
    Opcode reqOpCode = operation.getOpcode();
    Request request = operationToRequest(operation, providerId, auth);
    Response response = null;
    try {
      response = requestClient.processRequest(request);
    } catch (IOException e) {
      throw new ClientException("error in processRequest", e);
    }
    return responseToResult(response, reqOpCode);
  }

  private Request operationToRequest(
      NativeOperation operation, ProviderId providerId, Authentication auth) {

    Opcode opcode = operation.getOpcode();
    final RequestBody body;
    try {
      body = contentConverter.operationToBody(operation);
    } catch (Exception e) {
      throw new InterfaceException(e);
    }
    RequestHeader header =
        RequestHeader.builder()
            .provider(providerId)
            .session(0) // no provisioning of sessions yet
            .contentType(contentConverter.bodyType())
            .acceptType(acceptConverter.bodyType())
            .authType(auth.getAuthType())
            .opcode(opcode)
            .build();
    return Request.builder().header(header).body(body).auth(auth.createRequestAuth()).build();
  }

  private NativeResult responseToResult(Response response, Opcode expectedOpcode) {
    ResponseStatus status = response.getHeader().getStatus();
    if (status != ResponseStatus.Success) {
      throw new ServiceException(status);
    }

    Opcode opcode = response.getHeader().getOpcode();
    if (opcode != expectedOpcode) {
      throw new InvalidServiceResponseTypeException(expectedOpcode, opcode);
    }

    try {
      return acceptConverter.bodyToResult(response.getBody(), opcode);
    } catch (Exception e) {
      throw new InterfaceException(e);
    }
  }

  void setMaxBodySize(long maxBodySize) {
    this.requestClient.setMaxBodySize(maxBodySize);
  }

  void setIpcHandler(IpcHandler ipcHandler) {
    this.requestClient.setIpcHandler(ipcHandler);
  }

  void setTimeout(Duration timeout) {
    this.requestClient.setTimeout(timeout);
  }
}
