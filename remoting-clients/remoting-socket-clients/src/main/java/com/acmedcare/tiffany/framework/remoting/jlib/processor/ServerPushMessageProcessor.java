package com.acmedcare.tiffany.framework.remoting.jlib.processor;

import com.acmedcare.tiffany.framework.remoting.android.HandlerMessageListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareLogger;
import com.acmedcare.tiffany.framework.remoting.jlib.AcmedcareRemoting;
import com.acmedcare.tiffany.framework.remoting.jlib.Serializables;
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

  public ServerPushMessageProcessor(AcmedcareRemoting acmedcareRemoting) {
    this.acmedcareRemoting = acmedcareRemoting;
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

        switch (type) {
          case SINGLE:
            final Message.SingleMessage message =
                Serializables.fromBytes(messageBytes, Message.SingleMessage.class);

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
        }

        response.setBody(BizResult.SUCCESS.bytes());

      } else {

        AcmedcareLogger.w(null, "Received Messgae With no header info.");
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
      //noinspection unchecked
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  BizResult.ExceptionWrapper.builder()
                      .message(e.getMessage())

                      .build())
              .build()
              .bytes());
    }

    return response;
  }
}
