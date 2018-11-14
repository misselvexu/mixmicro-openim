package com.acmedcare.framework.newim.master.processor;

import static com.acmedcare.framework.newim.MasterLogger.masterReplicaLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.InstanceNode;
import com.acmedcare.framework.newim.InstanceNode.NodeType;
import com.acmedcare.framework.newim.master.processor.body.MasterSyncClusterSessionBody;
import com.acmedcare.framework.newim.master.processor.header.MasterSyncClusterSessionHeader;
import com.acmedcare.framework.newim.master.replica.MasterSession;
import com.acmedcare.tiffany.framework.remoting.common.RemotingUtil;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;

/**
 * Master Sync Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
public class MasterSyncClusterSessionRequestProcessor implements NettyRequestProcessor {

  private final MasterSession masterSession;

  public MasterSyncClusterSessionRequestProcessor(MasterSession masterSession) {
    this.masterSession = masterSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    masterReplicaLog.info("收到Master副本同步数据请求");

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {
      MasterSyncClusterSessionHeader header =
          (MasterSyncClusterSessionHeader)
              remotingCommand.decodeCommandCustomHeader(MasterSyncClusterSessionHeader.class);

      Assert.notNull(header, "同步数据请求头不能为空");
      masterReplicaLog.info("同步请求头信息:{}", JSON.toJSONString(header));

      InstanceNode node =
          new InstanceNode(
              RemotingUtil.socketAddress2String(channelHandlerContext.channel().remoteAddress()),
              NodeType.MASTER);

      if (!masterSession.checkSyncDataVersion(node, header.getDataVersion())) {
        masterReplicaLog.warn("同步的数据版本小于副本已经同步的数据版本,放弃同步");
        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(ExceptionWrapper.builder().message("同步的数据版本小于副本已经同步的数据版本,放弃同步").build())
                .build()
                .bytes());
        return response;
      }

      // 解析数据
      byte[] data = remotingCommand.getBody();
      // decode json
      MasterSyncClusterSessionBody syncClusterSessionBody =
          JSON.parseObject(data, MasterSyncClusterSessionBody.class);

      masterSession.merge(node, syncClusterSessionBody, header.getDataVersion());
      masterReplicaLog.info("数据同步Merge完成");

      response.setBody(BizResult.SUCCESS.bytes());

    } catch (Exception e) {
      masterReplicaLog.error("Master副本数据同步失败", e);
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
