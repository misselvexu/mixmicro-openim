/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.DelivererMessage;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;

import java.util.List;

/**
 * {@link DelivererRepository}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-01.
 */
public interface DelivererRepository {

  /**
   * 保存投递信息
   *
   * @param delivererMessage 消息
   */
  void savePostedDelivererMessage(DelivererMessage delivererMessage);

  /**
   * 撤销投递请求
   *
   * @param passportId 通行证
   * @param messageId 消息编号
   */
  void revokerDelivererMessage(String passportId, Long messageId);

  /**
   * 查询待投递的消息列表
   *
   * @param namespace 名称空间
   * @param passportId 通行证
   * @param messageType 消息类型
   * @param rowSize 行数
   * @return 消息列表
   */
  List<DelivererMessageBean> fetchDelivererMessages(
      String namespace, String passportId, Message.MessageType messageType, int rowSize);

  /**
   * 确认投递消息Ack状态
   *
   * @param namespace 名称空间
   * @param passportId 通行证
   * @param messageId 消息id
   */
  void commitDelivererAckMessage(String namespace, String passportId, String messageId);

  /**
   * 查询待投递的消息列表
   *
   * @param rowSize 行数
   * @return 消息列表
   */
  List<DelivererMessageBean> fetchDelivererMessages(int rowSize);
}
