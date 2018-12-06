package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.processor.header.PushMessageReadStatusHeader;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Remoting Client Push Message Read Status Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
public class RemotingClientPushMessageReadStatusProcessor extends AbstractNormalRequestProcessor {

  private final MessageService messageService;

  public RemotingClientPushMessageReadStatusProcessor(
      MessageService messageService, IMSession imSession) {
    super(imSession);
    this.messageService = messageService;
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

      PushMessageReadStatusHeader header =
          (PushMessageReadStatusHeader)
              remotingCommand.decodeCommandCustomHeader(PushMessageReadStatusHeader.class);

      Assert.notNull(header, "推送消息已读状态请求头参数异常");

      MessageType messageType = header.decodeMessageType();

      switch (messageType) {
        case GROUP:
          this.messageService.updateGroupMessageReadStatus(
              header.getPassportId(), header.getSender(), header.getMessageId());
          break;
        case SINGLE:
          this.messageService.updateSingleMessageReadStatus(
              header.getPassportId(), header.getSender(), header.getMessageId());
          break;
      }

      // return success
      response.setBody(BizResult.SUCCESS.bytes());

    } catch (Exception e) {
      // exception
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(
                  ExceptionWrapper.builder().message(e.getMessage()).type(e.getClass()).build())
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
