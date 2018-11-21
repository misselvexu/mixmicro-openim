package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.protocol.request.MasterPushMessageHeader;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;

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
      masterClusterLog.info("Master分发消息请求头信息:{}", JSON.toJSONString(header));

      // TODO 解析头信息
      // TODO 解析消息信息
      // TODO 分发消息到客户端

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
