/*
 * Copyright 1999-2018 Acmedcare+ Holding Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.DelivererMessage;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.deliver.api.bean.DelivererMessageBean;
import com.acmedcare.framework.newim.storage.api.DelivererRepository;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.DelivererMessage.DelivererStatus.DELIVERED;
import static com.acmedcare.framework.newim.DelivererMessage.DelivererStatus.DELIVERING;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.DELIVERER_MESSAGE;

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
  public void savePostedDelivererMessage(DelivererMessage delivererMessage) {

    try {
      this.mongoTemplate.save(delivererMessage, DELIVERER_MESSAGE);
    } catch (Exception e) {
      throw new StorageException(e);
    }
  }

  /**
   * 撤销投递请求
   *
   * @param passportId 通行证
   * @param messageId 消息编号
   */
  @Override
  public void revokerDelivererMessage(String passportId, Long messageId) {

    try {

      Query query = new Query(Criteria.where("receiver").is(passportId).and("mid").is(messageId));

      Update update = new Update();
      update.set("delivererStatus", DelivererMessage.DelivererStatus.CANCEL);

      UpdateResult result = this.mongoTemplate.updateMulti(query, update, DELIVERER_MESSAGE);

      mongoLog.info(
          "更新投递记录状态，匹配行数:{} ,更新影响行数:{}", result.getMatchedCount(), result.getModifiedCount());

    } catch (Exception e) {
      throw new StorageException(e);
    }
  }

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
      String namespace, String passportId, Message.MessageType messageType, int rowSize) {

    try {

      Criteria readyCriteria =
          Criteria.where("receiver")
              .is(passportId)
              .and("namespace")
              .is(namespace)
              .and("messageType")
              .is(messageType)
              .and("delivererStatus")
              .in(DelivererMessage.DelivererStatus.READY);

      LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(5);

      Criteria deliveringCriteria =
          Criteria.where("receiver")
              .is(passportId)
              .and("namespace")
              .is(namespace)
              .and("messageType")
              .is(messageType)
              .and("delivererStatus")
              .in(DELIVERING)
              .and("deliveringTime")
              .lte(Date.from(timeoutTime.atZone(ZoneId.systemDefault()).toInstant()));

      Query query = new Query(new Criteria().orOperator(readyCriteria, deliveringCriteria));

      // delivererTime desc
      query.with(Sort.by(Sort.Order.desc("delivererTime"))).limit(rowSize);

      List<DelivererMessage> messages =
          this.mongoTemplate.find(query, DelivererMessage.class, DELIVERER_MESSAGE);

      mongoLog.info(
          "查询待投递的结果集:{}-{}-{},数量:{}", namespace, passportId, messageType, messages.size());

      List<String> selectedIds = Lists.newArrayList();
      messages.forEach(delivererMessage -> selectedIds.add(delivererMessage.getUuid()));

      if (selectedIds.size() > 0) {
        String ukey = UUID.randomUUID().toString();

        // update
        Query uq =
            new Query(
                Criteria.where("uuid")
                    .in(selectedIds.toArray())
                    // only can be updated once
                    .and("deliveringTime")
                    .lte(Date.from(timeoutTime.atZone(ZoneId.systemDefault()).toInstant())));

        Update selectedUpdate = new Update();
        selectedUpdate.set("delivererStatus", DELIVERING);
        selectedUpdate.set("deliveringTime", new Date());
        selectedUpdate.set("ukey", ukey);

        UpdateResult updateResult =
            this.mongoTemplate.updateMulti(uq, selectedUpdate, DELIVERER_MESSAGE);

        mongoLog.info(
            "更新投递记录状态和时间，匹配行数:{} ,更新影响行数:{}",
            updateResult.getMatchedCount(),
            updateResult.getModifiedCount());

        // re-query
        Query mq = new Query(Criteria.where("uuid").in(selectedIds.toArray()).and("ukey").is(ukey));

        List<DelivererMessage> updatedMessages =
            this.mongoTemplate.find(mq, DelivererMessage.class, DELIVERER_MESSAGE);

        mongoLog.info(
            "查询待投递更新后的结果集:{}-{}-{},数量:{}",
            namespace,
            passportId,
            messageType,
            updatedMessages.size());

        // match

        List<DelivererMessageBean> list = Lists.newArrayList();

        messages.forEach(
            delivererMessage -> {
              if (updatedMessages.contains(delivererMessage)) {
                list.add(
                    DelivererMessageBean.builder()
                        .half(
                            delivererMessage
                                .getDelivererStatus()
                                .equals(DelivererMessage.DelivererStatus.HALF))
                        .message(delivererMessage.getPayload())
                        .messageId(delivererMessage.getMid().toString())
                        .messageType(delivererMessage.getMessageType())
                        .namespace(delivererMessage.getNamespace())
                        .passportId(delivererMessage.getReceiver())
                        .build());
              }
            });

        mongoLog.info(
            "查询待投递最终结果集:{}-{}-{}, 数量:{}",
            namespace,
            passportId,
            messageType,
            updatedMessages.size());

        return list;
      } else {

        return Lists.newArrayList();
      }
    } catch (Exception e) {
      throw new StorageException("查询待投递的消息列表异常", e);
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public List<DelivererMessageBean> fetchDelivererMessages(int rowSize) {
    try {

      Criteria readyCriteria =
          Criteria.where("delivererStatus").in(DelivererMessage.DelivererStatus.READY);

      LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(5);

      Criteria deliveringCriteria =
          Criteria.where("delivererStatus")
              .in(DELIVERING)
              .and("deliveringTime")
              .lte(Date.from(timeoutTime.atZone(ZoneId.systemDefault()).toInstant()));

      Query query = new Query(new Criteria().orOperator(readyCriteria, deliveringCriteria));

      // delivererTime desc
      query.with(Sort.by(Sort.Order.desc("delivererTime"))).limit(rowSize);

      List<DelivererMessage> messages =
          this.mongoTemplate.find(query, DelivererMessage.class, DELIVERER_MESSAGE);

      mongoLog.info(
          "[2]查询待投递的结果集,数量:{}", messages.size());

      List<String> selectedIds = Lists.newArrayList();
      messages.forEach(delivererMessage -> selectedIds.add(delivererMessage.getUuid()));

      if (selectedIds.size() > 0) {
        String ukey = UUID.randomUUID().toString();

        // update
        Query uq =
            new Query(
                Criteria.where("uuid")
                    .in(selectedIds.toArray())
                    // only can be updated once
                    .and("deliveringTime")
                    .lte(Date.from(timeoutTime.atZone(ZoneId.systemDefault()).toInstant())));

        Update selectedUpdate = new Update();
        selectedUpdate.set("delivererStatus", DELIVERING);
        selectedUpdate.set("deliveringTime", new Date());
        selectedUpdate.set("ukey", ukey);

        UpdateResult updateResult = this.mongoTemplate.updateMulti(uq, selectedUpdate, DELIVERER_MESSAGE);

        mongoLog.info(
            "[2]更新投递记录状态和时间，匹配行数:{} ,更新影响行数:{}", updateResult.getMatchedCount(), updateResult.getModifiedCount());

        // re-query
        Query mq = new Query(Criteria.where("uuid").in(selectedIds.toArray()).and("ukey").is(ukey));

        List<DelivererMessage> updatedMessages =
            this.mongoTemplate.find(mq, DelivererMessage.class, DELIVERER_MESSAGE);

        mongoLog.info("[2]查询待投递更新后的结果集,数量:{}", updatedMessages.size());

        // match

        List<DelivererMessageBean> list = Lists.newArrayList();

        messages.forEach(
            delivererMessage -> {
              if (updatedMessages.contains(delivererMessage)) {
                list.add(
                    DelivererMessageBean.builder()
                        .half(
                            delivererMessage
                                .getDelivererStatus()
                                .equals(DelivererMessage.DelivererStatus.HALF))
                        .message(delivererMessage.getPayload())
                        .messageId(delivererMessage.getMid().toString())
                        .messageType(delivererMessage.getMessageType())
                        .namespace(delivererMessage.getNamespace())
                        .passportId(delivererMessage.getReceiver())
                        .build());
              }
            });

        mongoLog.info("[2]查询待投递最终结果集数量:{}", updatedMessages.size());

        return list;
      } else {

        return Lists.newArrayList();
      }

    } catch (Exception e) {
      throw new StorageException("[2]查询待投递的消息列表异常", e);
    }
  }

  /**
   * 确认投递消息Ack状态
   *
   * @param namespace 名称空间
   * @param passportId 通行证
   * @param messageId 消息id
   */
  @Override
  public void commitDelivererAckMessage(String namespace, String passportId, String messageId) {

    try {

      mongoLog.info("确定投递记录状态参数:{},{},{}", namespace, passportId, messageId);
      Query query =
          new Query(
              Criteria.where("namespace")
                  .is(namespace)
                  .and("receiver")
                  .is(passportId)
                  .and("mid")
                  .is(Long.parseLong(messageId)));

      Update update = new Update();
      update.set("delivererStatus", DELIVERED);
      update.set("deliveredTime", new Date());

      UpdateResult updateResult = this.mongoTemplate.updateMulti(query, update, DELIVERER_MESSAGE);

      mongoLog.info(
          "确定投递记录状态和时间，匹配行数:{} ,更新影响行数:{}",
          updateResult.getMatchedCount(),
          updateResult.getModifiedCount());

    } catch (Exception e) {
      throw new StorageException("确认投递消息Ack状态异常", e);
    }
  }
}
