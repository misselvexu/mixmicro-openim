package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Remoting Client Push Message Read Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPushMessageReadStatusProcessor implements NettyRequestProcessor {

  private final MessageService messageService;

  public RemotingClientPushMessageReadStatusProcessor(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    // TODO 上报消息读取状态处理

    return null;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
