package com.acmedcare.framework.newim.server.processor;

import static com.acmedcare.framework.newim.server.ClusterLogger.masterClusterLog;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.protocol.request.MasterNoticeSessionDataBody;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 15/11/2018.
 */
public class MasterNoticeClientChannelsRequestProcessor implements NettyRequestProcessor {

  private final IMSession imSession;

  public MasterNoticeClientChannelsRequestProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    masterClusterLog.info("接收到Master服务器分发全局链接");
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      byte[] sessions = remotingCommand.getBody();
      masterClusterLog.debug("接受到的同步的数据为:{}", new String(sessions, "UTF-8"));
      MasterNoticeSessionDataBody noticeSessionDataBody =
          JSON.parseObject(sessions, MasterNoticeSessionDataBody.class);

      this.imSession.diff(
          noticeSessionDataBody.getPassportsConnections(),
          noticeSessionDataBody.getDevicesConnections());

      response.setBody(BizResult.builder().code(0).build().bytes());
    } catch (Exception e) {
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

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
