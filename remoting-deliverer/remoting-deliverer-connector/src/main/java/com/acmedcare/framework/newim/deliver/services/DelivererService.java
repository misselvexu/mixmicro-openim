/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.services;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.newim.DelivererMessage;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.request.TimedDelivererMessageRequestBean;
import com.acmedcare.framework.newim.deliver.connector.listener.event.DelivererEvent;
import com.acmedcare.framework.newim.deliver.context.ConnectorContext;
import com.acmedcare.framework.newim.storage.api.DelivererRepository;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * {@link DelivererService}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-08.
 */
@Service
public class DelivererService {

  private static final Logger log = LoggerFactory.getLogger(DelivererService.class);

  private final DelivererRepository delivererRepository;

  public DelivererService(DelivererRepository delivererRepository) {
    this.delivererRepository = delivererRepository;
  }

  // ====== Post Deliverer Message Method ======

  /**
   * Post Deliverer Message
   * @param half half message flag
   * @param namespace message namespace
   * @param passportId password id
   * @param clientType client type
   * @param messageType message type
   * @param message message payload
   */
  public void postDelivererMessage(boolean half, String namespace, String passportId, String clientType, Message.MessageType messageType, byte[] message) {

    Message originMessage = JSON.parseObject(message, Message.class);

    Assert.notNull(originMessage, "deliverer service post message payload must not be null .");

    log.info("[==] Deliverer Service post message : {} ,{} ,{} ,{}, {}" , half, namespace, passportId, messageType, originMessage.getMid());

    DelivererMessage delivererMessage = DelivererMessage.builder()
        .namespace(namespace)
        .mid(originMessage.getMid())
        .delivererStatus(half ? DelivererMessage.DelivererStatus.HALF : DelivererMessage.DelivererStatus.READY)
        .delivererType(DelivererMessage.DelivererType.OFFLINE)
        .messageType(messageType)
        .payload(message)
        .receiver(passportId)
        .clientType(clientType)
        .delivererTime(new Date())
        .build();

    this.delivererRepository.savePostedDelivererMessage(delivererMessage);

  }

  /**
   * Revoker Deliverer Request
   * @param passportId passport id
   * @param messageId message id
   */
  public void revokerDelivererMessage(String passportId, Long messageId) {

    this.delivererRepository.revokerDelivererMessage(passportId,messageId);

  }

  /**
   * 客户端处理服务端分发的定时投递消息到终端客户端
   * @param messages 消息列表
   */
  public void postTimerDelivererMessage(List<TimedDelivererMessageRequestBean.TimedMessage> messages) {
    ConnectorContext.context().publishEvent(DelivererEvent.TIMED_DELIVERER_MESSAGES_EVENT,messages);
  }

  /**
   * 获取需要投递的消息
   * @param namespace 名称空间
   * @param passportId 通行证编号
   * @param messageType 消息类型
   * @param rowSize 行数
   * @return 列表
   */
  public List<DelivererMessageBean> fetchDelivererMessages(String namespace, String passportId, Message.MessageType messageType, int rowSize) {
    return this.delivererRepository.fetchDelivererMessages(namespace,passportId,messageType,rowSize);
  }

  /**
   * 获取需要投递的消息
   * @param rowSize 行数
   * @return 列表
   */
  public List<DelivererMessageBean> fetchDelivererMessages(int rowSize) {
    return this.delivererRepository.fetchDelivererMessages(rowSize);
  }

  /**
   * 确认Ack消息
   * @param namespace 名称空间
   * @param passportId 通行证编号
   * @param messageId 消息编号
   */
  public void commitDelivererAckMessage(String namespace, String passportId, String messageId) {

    this.delivererRepository.commitDelivererAckMessage(namespace,passportId,messageId);

  }

}
