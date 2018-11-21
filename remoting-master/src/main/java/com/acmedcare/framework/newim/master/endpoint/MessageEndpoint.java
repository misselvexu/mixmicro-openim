package com.acmedcare.framework.newim.master.endpoint;

import static com.acmedcare.framework.newim.MasterLogger.endpointLog;

import com.acmedcare.framework.exception.defined.InvalidRequestParamException;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.client.EndpointConstants.MessageRequest;
import com.acmedcare.framework.newim.client.bean.request.BatchSendMessageRequest;
import com.acmedcare.framework.newim.client.bean.request.SendGroupMessageRequest;
import com.acmedcare.framework.newim.client.bean.request.SendMessageRequest;
import com.acmedcare.framework.newim.master.services.MessageServices;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Message Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@RestController
public class MessageEndpoint {

  // ========================= Inject Bean Defined =============================

  private final MessageServices messageServices;

  @Autowired
  public MessageEndpoint(MessageServices messageServices) {
    this.messageServices = messageServices;
  }

  // ========================= Request Mapping Method ==========================

  @PostMapping(MessageRequest.SEND_MESSAGE)
  ResponseEntity sendMessage(@RequestBody SendMessageRequest request) {
    try {
      endpointLog.info("send message request params: {}", JSON.toJSONString(request));
      this.messageServices.sendMessage(
          request.attribute(),
          request.getSender(),
          request.getReceiver(),
          request.getType(),
          request.getContent());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("send message failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(MessageRequest.BATCH_SEND_MESSAGE)
  ResponseEntity sendMessage(@RequestBody BatchSendMessageRequest request) {
    try {
      endpointLog.info("batch send message request params: {}", JSON.toJSONString(request));
      this.messageServices.sendMessage(
          request.attribute(),
          request.getSender(),
          request.getReceivers(),
          request.getType(),
          request.getContent());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("batch send message failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(MessageRequest.SEND_GROUP_MESSAGE)
  ResponseEntity sendGroupMessage(@RequestBody SendGroupMessageRequest request) {
    try {
      endpointLog.info("send group message request params: {}", JSON.toJSONString(request));

      this.messageServices.sendGroupMessage(
          request.attribute(),
          request.getSender(),
          request.getGroupId(),
          request.getType(),
          request.getContent());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("send group message failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }
}
