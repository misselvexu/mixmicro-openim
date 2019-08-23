/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.deliver.api.header.MessageHeader;
import com.acmedcare.framework.newim.deliver.api.request.CommitDelivererAckMessageRequestBean;
import com.acmedcare.framework.newim.deliver.api.request.DelivererMessageRequestBean;
import com.acmedcare.framework.newim.deliver.api.request.MessageRequestBean;
import com.acmedcare.framework.newim.deliver.api.response.MessageResponseBean;
import com.acmedcare.framework.newim.deliver.connector.client.DelivererClientProperties;
import com.acmedcare.framework.newim.deliver.context.ConnectorConnection;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.spi.Extension;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.acmedcare.framework.newim.deliver.api.DelivererCommand.*;

/**
 * {@link DefaultDelivererApi}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Extension("normal")
public class DefaultDelivererApi implements RemotingDelivererApi {

  private static final Logger log = LoggerFactory.getLogger(DefaultDelivererApi.class);

  // ==== Remoting Deliverer Executor Api ====

  @Override
  public void postDelivererMessage(
      boolean half,
      String namespace,
      String passportId,
      Message.MessageType messageType,
      byte[] message)
      throws RemotingDelivererException {

    try {

      DelivererClientProperties properties =
          ConnectorContext.context().getBean(DelivererClientProperties.class);

      ConnectorConnection connection = ConnectorContext.context().getAvailableConnection();

      MessageHeader header = MessageHeader.builder().build();

      RemotingCommand command = RemotingCommand.createRequestCommand(REQUEST_DELIVERER_VALUE, header);

      DelivererMessageRequestBean bean = DelivererMessageRequestBean.builder().messageType(messageType)
          .half(half).namespace(namespace).message(message).passportId(passportId).build();

      command.setBody(JSON.toJSONBytes(bean));

      RemotingCommand response = connection.execute(command, properties.getRequestTimeout());

      if (response != null) {

        BizResult result = BizResult.fromBytes(response.getBody(), BizResult.class);

        if (result.getCode() == 0) {
          log.info("[==] Deliverer Client post message succeed .");
          return;
        } else {
          throw new RemotingDelivererException("[==] Deliverer Client post message failed with received server response code : " + result.getCode());
        }
      }

      throw new RemotingDelivererException("[==] Deliverer Client post message failed without received server response");
    } catch (RemotingDelivererException e) {
      throw e;
    } catch (Exception e) {
      throw new RemotingDelivererException(e);
    }
  }

  /**
   * 提交投递消息Ack结果
   *
   * @param namespace  名称空间
   * @param messageId  消息编号
   * @param passportId 接收人编号
   */
  @Override
  public void commitDelivererMessage(String namespace, String messageId, String passportId) {

    try {

      DelivererClientProperties properties =
          ConnectorContext.context().getBean(DelivererClientProperties.class);

      ConnectorConnection connection = ConnectorContext.context().getAvailableConnection();

      RemotingCommand command = RemotingCommand.createRequestCommand(DELIVERER_CLIENT_ACK_DELIVERER_VALUE, null);

      CommitDelivererAckMessageRequestBean bean = CommitDelivererAckMessageRequestBean.builder().messageId(messageId).namespace(namespace).passportId(passportId).build();

      command.setBody(JSON.toJSONBytes(bean));

      RemotingCommand response = connection.execute(command, properties.getRequestTimeout());

      if (response != null) {

        BizResult result = BizResult.fromBytes(response.getBody(), BizResult.class);

        if (result.getCode() == 0) {
          log.info("[==] Deliverer Client commit ack message succeed .");
          return;
        } else {
          throw new RemotingDelivererException("[==] Deliverer Client commit ack message failed with received server response code : " + result.getCode());
        }
      }

      throw new RemotingDelivererException("[==] Deliverer Client commit ack message failed without received server response");
    } catch (RemotingDelivererException e) {
      throw e;
    } catch (Exception e) {
      throw new RemotingDelivererException(e);
    }

  }

  @Override
  public List<DelivererMessageBean> fetchClientDelivererMessage(String namespace, String passportId, Message.MessageType messageType) {

    try {

      DelivererClientProperties properties =
          ConnectorContext.context().getBean(DelivererClientProperties.class);

      ConnectorConnection connection = ConnectorContext.context().getAvailableConnection();

      RemotingCommand command = RemotingCommand.createRequestCommand(FETCH_CLIENT_DELIVERER_MESSAGES_VALUE,MessageHeader.builder().build());

      MessageRequestBean bean = MessageRequestBean.builder().namespace(namespace).passportId(passportId).messageType(messageType).build();

      command.setBody(JSON.toJSONBytes(bean));

      RemotingCommand response = connection.execute(command, properties.getRequestTimeout());

      if (response != null) {

        BizResult result = BizResult.fromBytes(response.getBody(), BizResult.class);

        if (result.getCode() == 0) {

          log.info("[==] Fetch Deliverer message list succeed .");

          MessageResponseBean responseBean = JSON.parseObject(JSON.toJSONString(result.getData()),MessageResponseBean.class);

          if(responseBean != null) {
            return responseBean.getMessages();
          } else {
            throw new RemotingDelivererException("[==] Fetch Deliverer message list response process failed with received server response code : " + result.getException());
          }

        } else {
          throw new RemotingDelivererException("[==] Fetch Deliverer message list response process failed with received server response code : " + result.getCode());
        }
      }

      throw new RemotingDelivererException("[==] Deliverer Client commit ack message failed without received server response");
    } catch (RemotingDelivererException e) {
      throw e;
    } catch (Exception e) {
      throw new RemotingDelivererException(e);
    }
  }
}
