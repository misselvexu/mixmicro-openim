package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.bean.Session;
import com.acmedcare.microservices.im.biz.request.PullSessionStatusHeader;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;

/**
 * Pull Session Status
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 10/08/2018.
 */
public class PullSessionStatusProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      PullSessionStatusHeader pullSessionHeader =
          (PullSessionStatusHeader)
              remotingCommand.decodeCommandCustomHeader(PullSessionStatusHeader.class);
      if (pullSessionHeader == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("拉取群组会话状态需要参数")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      // 查询当前用户名下的会话信息
      Session list =
          Datas.persistenceExecutor.queryAccountSessionStatus(
              pullSessionHeader.getUsername(),
              pullSessionHeader.getType(),
              pullSessionHeader.getFlagId());

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
