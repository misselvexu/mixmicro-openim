package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.imServerLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.server.RemotingWssServer.Ids;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PushMessageHeader;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSerializable;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;

/**
 * Client Push Message Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class RemotingClientPushMessageProcessor extends AbstractNormalRequestProcessor {

  private final MessageService messageService;

  public RemotingClientPushMessageProcessor(IMSession imSession, MessageService messageService) {
    super(imSession);
    this.messageService = messageService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      // valid
      RemotePrincipal principal = validatePrincipal(channelHandlerContext.channel());

      PushMessageHeader header =
          (PushMessageHeader) remotingCommand.decodeCommandCustomHeader(PushMessageHeader.class);

      Assert.notNull(header, "发送消息请求头参数异常");

      MessageType messageType = header.decodeType();

      byte[] messageBytes = remotingCommand.getBody();

      imServerLog.info(
          "[NEW-IM-CLIENT] 客户端请求发送消息:" + new String(messageBytes, Message.DEFAULT_CHARSET));

      // build result for client
      JSONObject ret = new JSONObject();
      switch (messageType) {
        case SINGLE:
          SingleMessage singleMessage =
              RemotingSerializable.decode(messageBytes, SingleMessage.class);

          long mid = Ids.snowflake.nextId();
          ret.put("mid", mid);
          singleMessage.setMid(mid);

          imServerLog.info("[NEW-IM-CLIENT] 服务器处理客户端消息,开始发送单聊消息给接受客户端");
          this.messageService.processMessage(imSession, singleMessage);
          break;

        case GROUP:
          GroupMessage groupMessage = RemotingSerializable.decode(messageBytes, GroupMessage.class);

          mid = Ids.snowflake.nextId();
          ret.put("mid", mid);
          groupMessage.setMid(mid);

          imServerLog.info("[NEW-IM-CLIENT] 服务器处理客户端消息,开始发送群组消息给接受客户端");
          this.messageService.processMessage(imSession, groupMessage);

          break;
      }

      response.setBody(BizResult.builder().code(0).data(ret).build().bytes());

    } catch (Exception e) {
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder()
                      .message(e.getMessage())
                      .type(e.getCause().getClass())
                      .build())
              .build()
              .bytes());
    }
    return response;
  }
}
