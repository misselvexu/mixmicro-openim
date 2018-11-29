package com.acmedcare.framework.newim.master.processor;

import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public class DefaultMasterProcessor implements NettyRequestProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultMasterProcessor.class);

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    LOG.warn("[NEW-IM] Default processor code:{} executing", remotingCommand.getCode());
    return RemotingCommand.createResponseCommand(remotingCommand.getCode(), "DEFAULT");
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
