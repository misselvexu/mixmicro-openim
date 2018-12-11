package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.protocol.request.MasterPushMessageHeader;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 15/11/2018.
 */
public class MasterPushMessageRequestProcessor implements NettyRequestProcessor {

  private final IMSession imSession;

  public MasterPushMessageRequestProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    masterClusterLog.info("接收到Master服务器分发消息请求");
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {
      MasterPushMessageHeader header =
          (MasterPushMessageHeader)
              remotingCommand.decodeCommandCustomHeader(MasterPushMessageHeader.class);
      Assert.notNull(header, "Master服务器分发消息请求头不能为空");
      masterClusterLog.info("Cluster接收到Master分发消息请求头信息:{}", JSON.toJSONString(header));

      MessageType messageType = header.decodeMessageType();
      masterClusterLog.info("Cluster接收到Master分发消息解析出消息的类型为:{}", messageType);
      byte[] message = remotingCommand.getBody();
      masterClusterLog.info(
          "Cluster接收到Master分发消息解析出的消息内容为:{}", new String(message, Message.DEFAULT_CHARSET));

      switch (messageType) {
        case GROUP:
          GroupMessage groupMessage = JSON.parseObject(message, GroupMessage.class);
          List<String> receivers = Lists.newArrayList(groupMessage.getReceivers());
          groupMessage.getReceivers().clear();
          imSession.sendMessageToPassport(header.getNamespace(), receivers, messageType, message);
          break;
        case SINGLE:
          SingleMessage singleMessage = JSON.parseObject(message, SingleMessage.class);
          imSession.sendMessageToPassport(
              header.getNamespace(), singleMessage.getReceiver(), messageType, message);
          break;
      }

      response.setBody(BizResult.builder().code(0).build().bytes());

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

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
