package com.acmedcare.microservices.im.core.processors;

import com.acmedcare.microservices.im.RemotingApplication.Ids;
import com.acmedcare.microservices.im.biz.BizResult;
import com.acmedcare.microservices.im.biz.BizResult.ExceptionWrapper;
import com.acmedcare.microservices.im.biz.bean.Message.GroupMessage;
import com.acmedcare.microservices.im.biz.bean.Message.MessageType;
import com.acmedcare.microservices.im.biz.bean.Message.SingleMessage;
import com.acmedcare.microservices.im.biz.request.ClientPushMessageHeader;
import com.acmedcare.microservices.im.core.ServerFacade;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSerializable;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.netty.channel.ChannelHandlerContext;

/**
 * Client Push Message
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 10/08/2018.
 */
public class ClientPushMessage implements NettyRequestProcessor {

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      ClientPushMessageHeader pushMessageHeader =
          (ClientPushMessageHeader)
              remotingCommand.decodeCommandCustomHeader(ClientPushMessageHeader.class);
      if (pushMessageHeader == null) {

        response.setBody(
            BizResult.builder()
                .code(-1)
                .exception(
                    ExceptionWrapper.builder()
                        .message("客户端发送消息参数异常")
                        .type(NullPointerException.class)
                        .build())
                .build()
                .bytes());

        return response;
      }

      MessageType messageType = pushMessageHeader.decodeType();

      byte[] messageBytes = remotingCommand.getBody();

      System.out.println("客户端请求发送消息:" + new String(messageBytes));

      JSONObject ret = new JSONObject();
      switch (messageType) {
        case SINGLE:
          SingleMessage singleMessage =
              RemotingSerializable.decode(messageBytes, SingleMessage.class);

          long mid = Ids.idHelper.nextId();
          ret.put("mid", mid);
          singleMessage.setMid(mid);

          ServerFacade.Executor.sendMessageAsync(Lists.newArrayList(singleMessage));
          break;

        case GROUP:
          GroupMessage groupMessage = RemotingSerializable.decode(messageBytes, GroupMessage.class);

          mid = Ids.idHelper.nextId();
          ret.put("mid", mid);
          groupMessage.setMid(mid);

          ServerFacade.Executor.sendMessageAsync(Lists.newArrayList(groupMessage));

          break;
      }

      response.setBody(BizResult.builder().data(ret).build().bytes());

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
