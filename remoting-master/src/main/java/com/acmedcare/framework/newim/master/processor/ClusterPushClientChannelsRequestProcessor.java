package com.acmedcare.framework.newim.master.processor;

import static com.acmedcare.framework.newim.MasterLogger.masterClusterAcceptorLog;
import static com.acmedcare.framework.newim.master.core.MasterSession.MasterClusterSession.CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.master.core.MasterSession.MasterClusterSession;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataBody;
import com.acmedcare.framework.newim.protocol.request.ClusterPushSessionDataHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;

/**
 * Cluster Push Client Channels Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 15/11/2018.
 */
public class ClusterPushClientChannelsRequestProcessor implements NettyRequestProcessor {

  private final MasterClusterSession masterClusterSession;

  public ClusterPushClientChannelsRequestProcessor(MasterClusterSession masterClusterSession) {
    this.masterClusterSession = masterClusterSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);
    InstanceNode node =
        channelHandlerContext.channel().attr(CLUSTER_INSTANCE_NODE_ATTRIBUTE_KEY).get();

    if (node == null) {
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(ExceptionWrapper.builder().message("未注册的客户端").build())
              .build()
              .bytes());
      return response;
    }
    masterClusterAcceptorLog.info("收到Cluster:{},上报数据请求", node.getHost());

    try {
      ClusterPushSessionDataHeader header =
          (ClusterPushSessionDataHeader)
              remotingCommand.decodeCommandCustomHeader(ClusterPushSessionDataHeader.class);

      Assert.notNull(header, "Cluster:" + node.getHost() + "请求上报数据请求头参数异常");

      ClusterPushSessionDataBody data =
          JSON.parseObject(remotingCommand.getBody(), ClusterPushSessionDataBody.class);

      masterClusterSession.merge(node, data);

      masterClusterAcceptorLog.info("Cluster:{},同步数据完成", node.getHost());

      response.setBody(BizResult.SUCCESS.bytes());

    } catch (Exception e) {
      masterClusterAcceptorLog.error("Cluster上报数据处理失败", e);
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder().message(e.getMessage()).type(e.getClass()).build())
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
