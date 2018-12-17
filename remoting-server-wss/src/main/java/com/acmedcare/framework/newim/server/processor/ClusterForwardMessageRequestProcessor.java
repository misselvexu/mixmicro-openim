package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.innerReplicaServerLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Cluster Forward Message Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class ClusterForwardMessageRequestProcessor extends AbstractClusterRequestProcessor {

  private final IMSession imSession;

  public ClusterForwardMessageRequestProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      ClusterForwardMessageHeader header =
          (ClusterForwardMessageHeader)
              remotingCommand.decodeCommandCustomHeader(ClusterForwardMessageHeader.class);

      Assert.notNull(header, "转发消息请求对象不能为空");
      MessageType messageType = header.decodeType();

      innerReplicaServerLog.info("[CLUSTER-PROCESSOR] 通讯服务器接收到转发消息的请求, 消息类型:{}", messageType);

      long start = System.currentTimeMillis();
      switch (messageType) {
        case SINGLE:
          SingleMessage singleMessage =
              JSON.parseObject(remotingCommand.getBody(), SingleMessage.class);
          String passportId = singleMessage.getReceiver();
          this.imSession.sendMessageToPassport(
              header.getNamespace(), passportId, messageType, singleMessage.bytes());

          break;
        case GROUP:
          GroupMessage groupMessage =
              JSON.parseObject(remotingCommand.getBody(), GroupMessage.class);
          List<String> passports = groupMessage.getReceivers();
          this.imSession.sendMessageToPassport(
              header.getNamespace(), passports, messageType, groupMessage.bytes());
          break;
      }

      innerReplicaServerLog.info(
          "[CLUSTER-PROCESSOR] 通讯服务器转发消息完成,耗时:{} ms", (System.currentTimeMillis() - start));

      // response
      response.setBody(BizResult.builder().code(0).build().bytes());

    } catch (Exception e) {
      innerReplicaServerLog.error("[CLUSTER-PROCESSOR] 通讯服务器转发消息处理异常", e);
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(ExceptionWrapper.builder().message(e.getMessage()).build())
              .build()
              .bytes());
    }
    return response;
  }
}
