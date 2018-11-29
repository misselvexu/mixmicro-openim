package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
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
    // TODO 处理Master推送来的 Session 数据
    return null;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
