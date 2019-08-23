package com.acmedcare.tiffany.framework.remoting.jlib.processor;

import com.acmedcare.tiffany.framework.remoting.android.HandlerMessageListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;
import com.acmedcare.tiffany.framework.remoting.jlib.*;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.BizResult;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.ServerPushMessageHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.events.AcmedcareEvent;

/**
 * Server Push Messages , Client Receive Message Listener
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
public class ServerPushMessageProcessor implements XLMRRequestProcessor {

  private final AcmedcareRemoting acmedcareRemoting;
  private final RemotingParameters parameters;

  public ServerPushMessageProcessor(AcmedcareRemoting acmedcareRemoting, RemotingParameters parameters) {
    this.acmedcareRemoting = acmedcareRemoting;
    this.parameters = parameters;
  }

  @Override
  public RemotingCommand processRequest(
      IoSession ioSession,
      RemotingCommand remotingCommand,
      HandlerMessageListener handlerMessageListener)
      throws Exception {

    AcmedcareLogger.i(null, "Received Server push message.");
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      Object o = remotingCommand.decodeCommandCustomHeader(ServerPushMessageHeader.class);

      if (o != null) {

        ServerPushMessageHeader header = (ServerPushMessageHeader) o;

        Message.MessageType type = header.decodeType();

        AcmedcareLogger.i(null, "Message Type :" + type.name());

        byte[] messageBytes = remotingCommand.getBody();
        AcmedcareLogger.i(
            this.getClass().getSimpleName(), "收到服务端推送的消息内容:" + new String(messageBytes));

        Long receivedMessageId = -1L;
        String namespace = Constants.DEFAULT_NAMESPACE;

        switch (type) {
          case SINGLE:
            final Message.SingleMessage message =
                Serializables.fromBytes(messageBytes, Message.SingleMessage.class);

            receivedMessageId = message.getMid();
            namespace = message.getNamespace();

            acmedcareRemoting
                .eventBus()
                .post(
                    new AcmedcareEvent() {
                      @Override
                      public Event eventType() {
                        return BizEvent.SERVER_PUSH_MESSAGE;
                      }

                      @Override
                      public Object data() {
                        return message;
                      }
                    });

            break;
          case GROUP:
            final Message.GroupMessage message2 =
                Serializables.fromBytes(messageBytes, Message.GroupMessage.class);

            receivedMessageId = message2.getMid();
            namespace = message2.getNamespace();

            acmedcareRemoting
                .eventBus()
                .post(
                    new AcmedcareEvent() {
                      @Override
                      public Event eventType() {
                        return BizEvent.SERVER_PUSH_MESSAGE;
                      }

                      @Override
                      public Object data() {
                        return message2;
                      }
                    });

            break;

          // @since 2.3.0
          case PUSH:
            final Message.PushMessage message3 =
                Serializables.fromBytes(messageBytes, Message.PushMessage.class);

            // push notify without ack .
            // receivedMessageId = message3.getMid();
            // namespace = message3.getNamespace();

            acmedcareRemoting
                .eventBus()
                .post(
                    new AcmedcareEvent() {
                      @Override
                      public Event eventType() {
                        return BizEvent.SERVER_NOTIFY_MESSAGE;
                      }

                      @Override
                      public Object data() {
                        return message3;
                      }
                    });
            break;

          default:
            AcmedcareLogger.w(null, "Un-supported message type :" + type);
            break;
        }

        // Add ack request
        if (receivedMessageId > 0 && parameters.isEnabledAck()) {
          final Long finalReceivedMessageId = receivedMessageId;
          final String finalNamespace = namespace;

          AsyncRuntimeExecutor.getAsyncThreadPool()
              .execute(
                  new Runnable() {
                    @Override
                    public void run() {
                      doAck(finalReceivedMessageId,parameters.getPassportId(), finalNamespace);
                    }
                  });
        }

        response.setBody(BizResult.SUCCESS.bytes());

      } else {

        AcmedcareLogger.w(null, "Received Message With no header info.");
        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    BizResult.ExceptionWrapper.<NullPointerException>builder()
                        .message("无法解析服务端推送的消息头信息")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());
        return response;
      }

    } catch (Exception e) {
      AcmedcareLogger.e(null, e, "Parse Received Server Message failed ");
      //noinspection un-checked
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(BizResult.ExceptionWrapper.builder().message(e.getMessage()).build())
              .build()
              .bytes());
    }

    return response;
  }

  private void doAck(Long finalReceivedMessageId, Long passportId, String namespace) {
    for (int i = 0; i < parameters.getAckRetryMaxTimes(); i++) {
      try{
        acmedcareRemoting.executor().ack0(namespace,finalReceivedMessageId,passportId);
        // if ack send ok , will break loop.
        break;
      } catch (Exception e) {
        AcmedcareLogger.w(null,"Client-[" + namespace + "] : [" + passportId + "] send-" + i + " message : [" + finalReceivedMessageId + "] failed , max times: " + parameters.getAckRetryMaxTimes());
        try {
          Thread.sleep(parameters.getAckRetryPeriod());
        } catch (InterruptedException ignored) {
        }
      }
    }

  }
}
