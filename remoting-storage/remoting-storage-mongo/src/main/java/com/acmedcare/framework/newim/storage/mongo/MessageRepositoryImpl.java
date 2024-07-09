package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.InnerType;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.MessageReadStatus;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import com.google.common.collect.Lists;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.*;

/**
 * Mongo Repository Implements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public class MessageRepositoryImpl implements MessageRepository {

  private final MongoTemplate mongoTemplate;
  private final TransactionTemplate transactionTemplate;

  public MessageRepositoryImpl(
      MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
    this.mongoTemplate = mongoTemplate;
    this.transactionTemplate = transactionTemplate;
  }

  /**
   * 保存信息
   *
   * @param message 信息
   * @return row
   */
  @Override
  public long saveMessage(Message message) {

    mongoLog.info("保存操作,消息类型:{} ,内容:{} ", IM_MESSAGE, message.toString());

    // check is saved
    boolean exist =
        mongoTemplate.exists(new Query(Criteria.where("mid").is(message.getMid())), IM_MESSAGE);

    if (!exist) {
      mongoTemplate.save(message, IM_MESSAGE);
    } else {
      mongoLog.warn("[NEW-IM-DB] 消息:{},已经存在,不重复添加", message.getMid());
    }
    return message.getMid();
  }

  /**
   * 批量保存消息
   *
   * @param messages 消息列表
   * @return 保存成功的消息
   */
  @Override
  public Long[] batchSaveMessage(Message... messages) {
    if (messages == null || messages.length == 0) {
      return new Long[0];
    }
    List<Long> saveMessageIds = Lists.newArrayList();
    for (Message message : messages) {
      saveMessageIds.add(message.getMid());
    }

    List<Long> existsIds =
        mongoTemplate.findDistinct(
            new Query(Criteria.where("mid").in(saveMessageIds)), "mid", IM_MESSAGE, Long.class);

    if (!existsIds.isEmpty()) {
      mongoLog.warn("[忽略]批量保存消息,部分消息已经存在,列表:{}", Arrays.toString(existsIds.toArray()));
    }

    List<Message> readySaveMessages = Lists.newArrayList();
    Arrays.stream(messages)
        .forEach(
            message -> {
              if (!existsIds.contains(message.getMid())) {
                readySaveMessages.add(message);
              }
            });

    mongoTemplate.insert(readySaveMessages, IM_MESSAGE);
    List<Long> result = Lists.newArrayList();
    readySaveMessages.forEach(message -> result.add(message.getMid()));

    mongoLog.info("批量保存成功的消息编号:{}", Arrays.toString(result.toArray()));

    return result.toArray(new Long[0]);
  }

  /**
   * 查询群组信息
   *
   * @param groupId 群组 ID
   * @param receiverId 接收人 ID
   * @param limit 查询条数
   * @param queryLeast 是否查询最新的消息
   * @param leastMessageId 客户端最新的消息编号
   * @return 列表
   */
  @Override
  public List<? extends Message> queryGroupMessages(
      String namespace,
      String groupId,
      String receiverId,
      long limit,
      boolean queryLeast,
      long leastMessageId) {

    // 用户是否属于该群组
    Query checkQuery =
        new Query(
            Criteria.where("groupId")
                .is(groupId)
                .and("memberId")
                .is(receiverId)
                .and("namespace")
                .is(namespace));
    boolean result = this.mongoTemplate.exists(checkQuery, REF_GROUP_MEMBER);
    if (!result) {
      mongoLog.warn("用户:{} ,不属于群组:{}", receiverId, groupId);
      return Lists.newArrayList();
    }

    // 默认查询最新的消息列表
    Query messageQuery =
        new Query(
            Criteria.where("group")
                .is(groupId)
                .and("namespace")
                .is(namespace)
                .and("messageType")
                .is(MessageType.GROUP.name()));
    messageQuery.with(new Sort(Direction.DESC, "sendTimestamp")).limit((int) limit);

    if (!queryLeast && leastMessageId > 0) {
      try {
        // query least message id innerTimestamp
        Query leastMessageIdQuery = new Query(Criteria.where("mid").is(leastMessageId)).limit(1);
        Message leastMessage =
            this.mongoTemplate.findOne(leastMessageIdQuery, Message.class, IM_MESSAGE);

        if (leastMessage != null) {

          mongoLog.info("查询群组历史消息,客户端最新的消息编号:{}", leastMessage.getMid());

          Date tempDate = leastMessage.getSendTimestamp();
          messageQuery =
              new Query(
                  Criteria.where("group")
                      .is(groupId)
                      .and("namespace")
                      .is(namespace)
                      .and("messageType")
                      .is(MessageType.GROUP.name())
                      .and("innerType")
                      .ne(InnerType.COMMAND)
                      .and("sendTimestamp") // 查询的消息的时间小于客户端执行的最新消息的时间
                      .lt(tempDate));
          messageQuery.with(new Sort(Direction.DESC, "sendTimestamp")).limit((int) limit);
        } else {
          mongoLog.warn("查询群组历史消息,客户端提供的最新消息编号:{} 无效", leastMessageId);
        }

      } catch (Exception e) {
        mongoLog.warn("检查最新的消息ID:" + leastMessageId + "异常,默认查询最新的消息", e);
      }
    }

    List<GroupMessage> groupMessages =
        this.mongoTemplate.find(messageQuery, GroupMessage.class, IM_MESSAGE);
    mongoLog.info("查询群组的消息数量:{}", groupMessages.size());

    return groupMessages;
  }

  /**
   * 查询消息列表
   *
   * @param sender 发送人
   * @param receiverId 接收人
   * @param limit 条数
   * @param queryLeast 是否是查询最新的消息
   * @param leastMessageId 客户端最新的消息编号
   * @return 列表
   */
  @Override
  public List<? extends Message> querySingleMessages(
      String namespace,
      String sender,
      String receiverId,
      long limit,
      boolean queryLeast,
      long leastMessageId) {
    mongoLog.info(
        "查询消息列表,参数:{},{},{},{},{}", sender, receiverId, limit, queryLeast, leastMessageId);

    // 默认查询最新的消息列表
    Query messageQuery =
        new Query(
            Criteria.where("sender")
                .is(sender)
                .and("receiver")
                .is(receiverId)
                .and("namespace")
                .is(namespace)
                .and("messageType")
                .is(MessageType.SINGLE.name()));
    messageQuery.with(new Sort(Direction.DESC, "sendTimestamp")).limit((int) limit);

    if (!queryLeast && leastMessageId > 0) {
      try {
        // query least message id innerTimestamp
        Query leastMessageIdQuery = new Query(Criteria.where("mid").is(leastMessageId)).limit(1);
        Message leastMessage =
            this.mongoTemplate.findOne(leastMessageIdQuery, Message.class, IM_MESSAGE);

        if (leastMessage != null) {

          mongoLog.info("查询历史消息,客户端最新的消息编号:{}", leastMessage.getMid());
//          long innerTimestamp = leastMessage.getInnerTimestamp();
          Date tempDate = leastMessage.getSendTimestamp();
          messageQuery =
              new Query(
                  Criteria.where("sender")
                      .is(sender)
                      .and("receiver")
                      .is(receiverId)
                      .and("namespace")
                      .is(namespace)
                      .and("messageType")
                      .is(MessageType.SINGLE.name())
                      .and("innerType")
                      .ne(InnerType.COMMAND)
                      .and("sendTimestamp") // 查询的消息的时间小于客户端执行的最新消息的时间
                      .lt(tempDate));
          messageQuery.with(new Sort(Direction.DESC, "sendTimestamp")).limit((int) limit);
        } else {
          mongoLog.warn("查询历史消息,客户端提供的最新消息编号:{}无效", leastMessageId);
        }

      } catch (Exception e) {
        mongoLog.warn("检查最新的消息ID:" + leastMessageId + "异常,默认查询最新的消息", e);
      }
    }

    List<SingleMessage> singleMessages =
        this.mongoTemplate.find(messageQuery, SingleMessage.class, IM_MESSAGE);
    mongoLog.info("查询的消息数量:{}", singleMessages.size());

    return singleMessages;
  }

  /**
   * 查询群组消息
   *
   * @param groupId 群组 ID
   * @param messageId 消息编号
   * @return 消息
   */
  @Override
  public GroupMessage queryGroupMessage(String groupId, String messageId) {
    Query query =
        new Query(
                Criteria.where("group")
                    .is(groupId)
                    .and("messageType")
                    .is(MessageType.GROUP.name())
                    .and("mid")
                    .is(Long.parseLong(messageId)))
            .limit(1);
    return this.mongoTemplate.findOne(query, GroupMessage.class, IM_MESSAGE);
  }

  /**
   * 更新群组消息的已读数和状态
   *
   * @param passportId 接收人编号
   * @param groupId 群组编号
   * @param messageId 消息编号
   * @param sendTimestamp 当前消息时间戳
   */
  @Override
  public void updateGroupMessageReadStatus(
      String passportId, String groupId, String messageId, Date sendTimestamp) {

    try {
      MessageReadStatus messageReadStatus = new MessageReadStatus();
      messageReadStatus.setGroupId(groupId);
      messageReadStatus.setMemberId(Long.parseLong(passportId));
      messageReadStatus.setMessageId(Long.parseLong(messageId));
      messageReadStatus.setReadTimestamp(new Date());

      // save
      mongoTemplate.insert(messageReadStatus, MESSAGE_READ_STATUS);

      // update
      Query query =
          new Query(Criteria.where("group").is(groupId).and("sendTimestamp").lte(sendTimestamp));

      Update update = new Update();
      update.inc("readedSize", 1);
      UpdateResult updateResult = mongoTemplate.updateMulti(query, update, IM_MESSAGE);

      mongoLog.info(
          "匹配行数:{} ,更新影响行数:{}", updateResult.getMatchedCount(), updateResult.getModifiedCount());

      if (updateResult.getModifiedCount() == 0) {
        throw new StorageException("更新消息主表已读数失败");
      }

    } catch (Exception e) {
      mongoLog.error("用户:{},更新群组:{},消息:{},已读数操作异常回滚", passportId, groupId, messageId, e);
    }

    /*
    mongoTemplate.setSessionSynchronization(ALWAYS);
    transactionTemplate.execute(
        new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
            try {
              MessageReadStatus messageReadStatus = new MessageReadStatus();
              messageReadStatus.setGroupId(groupId);
              messageReadStatus.setMemberId(Long.parseLong(passportId));
              messageReadStatus.setMessageId(Long.parseLong(messageId));
              messageReadStatus.setReadTimestamp(new Date());

              // save
              mongoTemplate.insert(messageReadStatus, MESSAGE_READ_STATUS);

              // update
              Query query =
                  new Query(
                      Criteria.where("group")
                          .is(groupId)
                          .and("innerTimestamp")
                          .lte(innerTimestamp));

              Update update = new Update();
              update.inc("readedSize", 1);
              UpdateResult updateResult = mongoTemplate.updateMulti(query, update, IM_MESSAGE);

              mongoLog.info(
                  "匹配行数:{} ,更新影响行数:{}",
                  updateResult.getMatchedCount(),
                  updateResult.getModifiedCount());

              if (updateResult.getModifiedCount() == 0) {
                throw new StorageException("更新消息主表已读数失败");
              }

            } catch (Exception e) {
              mongoLog.error("用户:{},更新群组:{},消息:{},已读数操作异常回滚", passportId, groupId, messageId, e);
              transactionStatus.setRollbackOnly();
            } finally {
              mongoTemplate.setSessionSynchronization(SessionSynchronization.ON_ACTUAL_TRANSACTION);
            }
          }
        });
        */
  }

  @Override
  public void updateSingleMessageReadStatus(String passportId, String sender, String messageId) {

    SingleMessage singleMessage = this.querySingleMessage(messageId);
    if (singleMessage == null) {
      throw new StorageException("无效的单聊消息ID");
    }

    Query query =
        new Query(
            Criteria.where("sender")
                .is(sender)
                .and("receiver")
                .is(passportId)
                .and("readFlag")
                .is(false)
                .and("messageType")
                .is(MessageType.SINGLE.name())
                .and("sendTimestamp")
                .lte(singleMessage.getSendTimestamp()));

    Update update = new Update();
    update.set("readFlag", true);
    UpdateResult updateResult = mongoTemplate.updateMulti(query, update, IM_MESSAGE);
    mongoLog.info(
        "用户:{}更新消息:{},已读状态,匹配行数:{},影响行数:{}",
        passportId,
        messageId,
        updateResult.getMatchedCount(),
        updateResult.getModifiedCount());
  }

  @Override
  public SingleMessage querySingleMessage(String messageId) {
    Query query =
        new Query(
                Criteria.where("mid")
                    .is(Long.parseLong(messageId))
                    .and("messageType")
                    .is(MessageType.SINGLE.name()))
            .limit(1);

    return mongoTemplate.findOne(query, SingleMessage.class, IM_MESSAGE);
  }

  /**
   * 查询已读人员的编号
   *
   * @param groupId 群组编号
   * @param messageId 消息编号
   * @return 人员 IDS
   */
  @Override
  public List<Long> queryGroupMessageReaders(String groupId, String messageId) {
    Query query =
        new Query(
            Criteria.where("groupId").is(groupId).and("messageId").is(Long.parseLong(messageId)));
    return mongoTemplate.findDistinct(query, "memberId", MESSAGE_READ_STATUS, Long.class);
  }
}
