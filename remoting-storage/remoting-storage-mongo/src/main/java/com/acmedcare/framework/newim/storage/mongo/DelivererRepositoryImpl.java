/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.DelivererMessage;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.storage.api.DelivererRepository;
import com.mongodb.MongoClient;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

/**
 * {@link DelivererRepositoryImpl}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2019-08-12.
 */
@Repository
public class DelivererRepositoryImpl implements DelivererRepository {

  private final MongoTemplate mongoTemplate;
  private final TransactionTemplate transactionTemplate;
  private final MongoClient mongoClient;

  public DelivererRepositoryImpl(
      MongoTemplate mongoTemplate,
      TransactionTemplate transactionTemplate,
      MongoClient mongoClient) {
    this.mongoTemplate = mongoTemplate;
    this.transactionTemplate = transactionTemplate;
    this.mongoClient = mongoClient;
  }

  /**
   * 保存投递信息
   *
   * @param delivererMessage 消息
   */
  @Override
  public void savePostedDelivererMessage(DelivererMessage delivererMessage) {}

  /**
   * 撤销投递请求
   *
   * @param passportId 通行证
   * @param messageId 消息编号
   */
  @Override
  public void revokerDelivererMessage(String passportId, Long messageId) {}

  /**
   * 查询待投递的消息列表
   *
   * @param namespace 名称空间
   * @param passportId 通行证
   * @param messageType 消息类型
   * @return 消息列表
   */
  @Override
  public List<DelivererMessageBean> fetchDelivererMessages(
      String namespace, String passportId, Message.MessageType messageType) {
    return null;
  }

  /**
   * 确认投递消息Ack状态
   *
   * @param namespace 名称空间
   * @param passportId 通行证
   * @param messageId 消息id
   */
  @Override
  public void commitDelivererAckMessage(String namespace, String passportId, String messageId) {}
}
