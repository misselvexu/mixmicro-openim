package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Default Processor
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public class DefaultProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    // default return
    return RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
