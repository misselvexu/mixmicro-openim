package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.biz.BizCode;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.request.AuthHeader;
import com.acmedcare.microservices.im.core.ServerFacade;
import com.acmedcare.microservices.im.kits.ThreadKit.WrapExceptionRunnable;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
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

    RemotingCommand response = RemotingCommand.createResponseCommand(BizCode.AUTH, null);

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

      if (ServerFacade.channelsMapping().containsKey(username)) {
        //       exist ,append new channel
        List<Channel> channels = ServerFacade.channelsMapping().get(username);

        /**
         * 通知客户端下线
         *
         * <pre>
         *
         * </pre>
         */
        if (channels != null && channels.size() > 0) {
          for (Channel channel : channels) {
            if (channel != null && channel.isOpen() && channel.isActive() && channel.isWritable()) {

              ServerFacade.submitTask(
                  new WrapExceptionRunnable(
                      new Runnable() {
                        @Override
                        public void run() {

                          RemotingCommand focusLogout =
                              RemotingCommand.createRequestCommand(
                                  BizCode.SERVER_PUSH_FOCUS_LOGOUT, null);

                          channel
                              .writeAndFlush(focusLogout)
                              .addListener(
                                  new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture future)
                                        throws Exception {
                                      if (future.isSuccess()) {
                                        try {
                                          // sleep & delay close
                                          Thread.sleep(3000);
                                        } finally {
                                          // close by server
                                          channel.close();
                                        }
                                      }
                                    }
                                  });
                        }
                      }));
            }
          }

          // clear all online clients
          channels.clear();
        }

        ServerFacade.channelsMapping().get(username).add(channelHandlerContext.channel());

      } else {
        // no exist, add new
        ServerFacade.channelsMapping()
            .put(username, Lists.newArrayList(channelHandlerContext.channel()));
      }

      System.out.println("客户端用户授权登录成功:" + username);
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
