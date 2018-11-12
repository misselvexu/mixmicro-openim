package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.request.PushMessageStatusHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Client Push Message Read Status
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public class PushMessageReadStatusProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      Object header = remotingCommand.decodeCommandCustomHeader(PushMessageStatusHeader.class);

      if (header == null) {
        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("推送消息状态变更请求头异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      PushMessageStatusHeader pushMessageStatusHeader = (PushMessageStatusHeader) header;

      boolean result =
          Datas.persistenceExecutor.saveOrUpdateMessageReadStatus(
              pushMessageStatusHeader.getUsername(),
              pushMessageStatusHeader.decodePMTValue(),
              pushMessageStatusHeader.getSender(),
              pushMessageStatusHeader.getLeastMessageId());

      response.setBody(result ? BizResult.SUCCESS.bytes() : BizResult.FAILED.bytes());

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
