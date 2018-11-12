package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.bean.Session;
import com.acmedcare.microservices.im.biz.request.PullSessionHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;

/**
 * Pull Session List
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public class PullSessionProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      Object o = remotingCommand.decodeCommandCustomHeader(PullSessionHeader.class);
      if (o == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组会话列表需要参数用户名")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      PullSessionHeader pullSessionHeader = (PullSessionHeader) o;
      System.out.println("请求拉取会话列表,用户名:" + pullSessionHeader.getUsername());

      // 查询当前用户名下的会话信息
      List<Session> list =
          Datas.persistenceExecutor.queryAccountSessions(pullSessionHeader.getUsername());

      System.out.println("查询会话列表返回值:" + JSON.toJSONString(list));

      response.setBody(BizResult.builder().code(0).data(list).build().bytes());

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
