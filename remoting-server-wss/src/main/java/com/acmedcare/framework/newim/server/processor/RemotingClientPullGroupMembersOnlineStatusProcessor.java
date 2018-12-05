package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Pull Group Members Online Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPullGroupMembersOnlineStatusProcessor implements NettyRequestProcessor {

  private final IMSession imSession;

  public RemotingClientPullGroupMembersOnlineStatusProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    // TODO 拉取群成员在线情况

    return null;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
