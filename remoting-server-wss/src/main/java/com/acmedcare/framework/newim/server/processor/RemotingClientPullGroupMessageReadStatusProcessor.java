package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Pull Group Message Read Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPullGroupMessageReadStatusProcessor implements NettyRequestProcessor {

  private final MessageService messageService;
  private final GroupService groupService;

  public RemotingClientPullGroupMessageReadStatusProcessor(
      MessageService messageService, GroupService groupService) {
    this.messageService = messageService;
    this.groupService = groupService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    // TODO 拉取群消息读取状态

    return null;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
