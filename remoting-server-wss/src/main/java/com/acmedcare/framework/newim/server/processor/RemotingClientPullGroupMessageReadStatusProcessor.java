package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.MessageReadStatus.MessageStatusDetail;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PullGroupMessageReadStatusHeader;
import com.acmedcare.framework.newim.server.service.GroupService;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Pull Group Message Read Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPullGroupMessageReadStatusProcessor
    extends AbstractNormalRequestProcessor {

  private final MessageService messageService;
  private final GroupService groupService;

  public RemotingClientPullGroupMessageReadStatusProcessor(
      MessageService messageService, GroupService groupService, IMSession imSession) {
    super(imSession);
    this.messageService = messageService;
    this.groupService = groupService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      // valid
      RemotePrincipal principal = validatePrincipal(channelHandlerContext.channel());

      PullGroupMessageReadStatusHeader header =
          (PullGroupMessageReadStatusHeader)
              remotingCommand.decodeCommandCustomHeader(PullGroupMessageReadStatusHeader.class);
      if (header == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组消息读取状态请求头参数异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      MessageStatusDetail detail =
          this.messageService.queryGroupMessageReadStatusList(
              header.getGroupId(), header.getMessageId());

      response.setBody(BizResult.builder().code(0).data(detail).build().bytes());

    } catch (Exception e) {
      // set error response
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
