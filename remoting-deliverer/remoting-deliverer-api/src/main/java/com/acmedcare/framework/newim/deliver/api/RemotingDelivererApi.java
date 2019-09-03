/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.deliver.api;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.deliver.api.exception.RemotingDelivererException;
import com.acmedcare.framework.newim.spi.Extensible;

import java.util.List;

/**
 * {@link RemotingDelivererApi}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-07-29.
 */
@Extensible
public interface RemotingDelivererApi {

  /**
   * 转发投递服务器
   *
   * @param half 是否是预转发,投递服务器需要进行内存预判操作，防止重复投递
   * @param namespace namespace
   * @param passportId passport Id
   * @param clientType  client type
   * @param messageType message type
   * @param message message content bytes
   * @throws RemotingDelivererException maybe thrown {@link RemotingDelivererException}
   * @since 2.3.0
   */
  void postDelivererMessage(
      boolean half,
      String namespace,
      String passportId,
      String clientType,
      Message.MessageType messageType,
      byte[] message)
      throws RemotingDelivererException;

  /**
   * 提交投递消息Ack结果
   *
   * @param namespace 名称空间
   * @param messageId 消息编号
   * @param passportId 接收人编号
   */
  void commitDelivererMessage(String namespace, String messageId, String passportId);

  /**
   * 获取客户端待投递消息
   * @param namespace 名称空间
   * @param passportId 通行证编号
   * @param messageType 消息类型
   * @return 消息列表
   */
  List<DelivererMessageBean> fetchClientDelivererMessage(String namespace, String passportId, Message.MessageType messageType);
}
