package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.biz.BizCode;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.bean.Account;
import com.acmedcare.microservices.im.biz.request.AuthHeader;
import com.acmedcare.microservices.im.core.ClientChannel;
import com.acmedcare.microservices.im.core.ServerFacade;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Auth Processor
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public class AuthProcessor implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response = RemotingCommand.createResponseCommand(BizCode.HEARTBEAT, null);

    try {

      AuthHeader authHeader =
          (AuthHeader) remotingCommand.decodeCommandCustomHeader(AuthHeader.class);

      if (authHeader == null || StringUtils.isBlank(authHeader.getUsername())) {
        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("授权协议需要客户端提供登录用户名,并且不能为空")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      // process auth
      String username = authHeader.getUsername();
      Account account = Account.builder().username(username).build();

      if (ServerFacade.channelsMapping().containsKey(account)) {
        // exist ,append new channel
        ServerFacade.channelsMapping()
            .get(account)
            .add(ClientChannel.builder().channel(channelHandlerContext.channel()).build());

      } else {

        // no exist, add new
        ServerFacade.channelsMapping()
            .put(
                account,
                Lists.newArrayList(
                    ClientChannel.builder().channel(channelHandlerContext.channel()).build()));
      }

      // success processed
      response.setBody(BizResult.SUCCESS.bytes());

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
