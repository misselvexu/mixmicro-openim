package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.server.processor.header.PullMessageHeader;
import com.acmedcare.framework.newim.server.service.MessageService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Remoting Client Pull Message Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class RemotingClientPullMessageProcessor implements NettyRequestProcessor {

  private final MessageService messageService;

  public RemotingClientPullMessageProcessor(MessageService messageService) {
    this.messageService = messageService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      Object header = remotingCommand.decodeCommandCustomHeader(PullMessageHeader.class);
      if (header == null) {
        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取消息列表请求头参数异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      PullMessageHeader pullMessageHeader = (PullMessageHeader) header;

      // 拉取用户组消息列表
      List<? extends Message> list =
          this.messageService.queryAccountMessages(
              pullMessageHeader.getPassport(),
              pullMessageHeader.getPassportId(),
              pullMessageHeader.getSender(),
              pullMessageHeader.getType(),
              pullMessageHeader.getLeastMessageId(),
              pullMessageHeader.getLimit());

      System.out.println("客户端拉取消息返回值:" + JSON.toJSONString(list));
      response.setBody(BizResult.builder().code(0).data(list).build().bytes());

    } catch (Exception e) {
      // exception
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
