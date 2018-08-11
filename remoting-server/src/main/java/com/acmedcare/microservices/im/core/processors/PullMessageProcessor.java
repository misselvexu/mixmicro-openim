package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.request.PullMessageHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Pull Message By Client
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public class PullMessageProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      //
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
      List<Message> list =
          Datas.persistenceExecutor.queryAccountGroupMessages(
              pullMessageHeader.getUsername(),
              pullMessageHeader.getSender(),
              pullMessageHeader.getType(),
              pullMessageHeader.getLeastMessageId(),
              pullMessageHeader.getLimit());

      System.out.println("客户端拉取消息返回值:" + JSON.toJSONString(list));
      response.setBody(
          BizResult.builder()
              .code(0)
//              .data(MessageBody.builder().messages(list).build())
              .data(list)
              .build()
              .bytes());

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
