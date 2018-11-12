package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Remoting Client Register Auth Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public class RemotingClientRegisterAuthProcessor implements NettyRequestProcessor {

  private IMSession imSession;

  public RemotingClientRegisterAuthProcessor(IMSession imSession) {
    this.imSession = imSession;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    return null;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
