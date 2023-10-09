package com.acmedcare.framework.newim.master.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.master.core.MasterSession.MasterClusterSession;
import com.acmedcare.framework.newim.protocol.request.ClusterForwardMessageHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.master.core.MasterSession.MasterClusterSession.decodeInstanceNode;

/**
 * Cluster Forward Message Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
public class ClusterForwardMessageRequestProcessor implements NettyRequestProcessor {

  private final MasterClusterSession masterClusterSession;

  public ClusterForwardMessageRequestProcessor(MasterClusterSession masterClusterSession) {
    this.masterClusterSession = masterClusterSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    InstanceNode instanceNode = decodeInstanceNode(channelHandlerContext.channel());

    if (instanceNode == null) {
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(ExceptionWrapper.builder().message("未注册的客户端").build())
              .build()
              .bytes());
      return response;
    }

    masterClusterAcceptorLog.info("收到Cluster:{},转发数据请求", instanceNode.getAddress());

    try {
      ClusterForwardMessageHeader header =
          (ClusterForwardMessageHeader)
              remotingCommand.decodeCommandCustomHeader(ClusterForwardMessageHeader.class);

      Assert.notNull(header, "Cluster:" + instanceNode.getAddress() + "请求上报数据请求头参数异常");

      byte[] message = remotingCommand.getBody();
      masterClusterAcceptorLog.info(
          "收到 Cluster 转发的消息内容:{}", new String(message, Message.DEFAULT_CHARSET));

      // 解析消息数据
      switch (header.decodeType()) {
        case SINGLE:
          SingleMessage singleMessage = JSON.parseObject(message, SingleMessage.class);
          MessageAttribute attribute =
              MessageAttribute.builder()
                  .namespace(header.getNamespace())
                  .maxRetryTimes(singleMessage.getMaxRetryTimes())
                  .persistent(singleMessage.isPersistent())
                  .qos(singleMessage.isQos())
                  .retryPeriod(singleMessage.getRetryPeriod())
                  .build();
          masterClusterSession.distributeMessage(attribute, singleMessage, instanceNode.getAddress());
          break;
        case GROUP:
          GroupMessage groupMessage = JSON.parseObject(message, GroupMessage.class);
          attribute =
              MessageAttribute.builder()
                  .namespace(header.getNamespace())
                  .maxRetryTimes(groupMessage.getMaxRetryTimes())
                  .persistent(groupMessage.isPersistent())
                  .qos(groupMessage.isQos())
                  .retryPeriod(groupMessage.getRetryPeriod())
                  .build();
          masterClusterSession.distributeMessage(attribute, groupMessage, instanceNode.getAddress());
          break;

        case MQ:
          MQMessage mqMessage = JSON.parseObject(message, MQMessage.class);
          attribute = MessageAttribute.builder().persistent(false).build();
          masterClusterAcceptorLog.info(
              "接受服务器转发请求:{},{}", instanceNode.getAddress(), mqMessage.toString());
          masterClusterSession.distributeMessage(attribute, mqMessage, instanceNode.getAddress());
          break;
        default:
          masterClusterAcceptorLog.info("无效的消息类型:{}", header.decodeType());
          break;
      }

      masterClusterAcceptorLog.info("Cluster:{},同步数据完成", instanceNode.getAddress());

      response.setBody(BizResult.SUCCESS.bytes());

    } catch (Exception e) {
      masterClusterAcceptorLog.error("Cluster转发消息数据处理失败", e);
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder().message(e.getMessage()).type(e.getClass()).build())
              .build()
              .bytes());
    }

    // final response result
    return response;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
