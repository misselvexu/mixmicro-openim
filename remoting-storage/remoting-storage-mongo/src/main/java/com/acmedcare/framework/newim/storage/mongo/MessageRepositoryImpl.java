package com.acmedcare.framework.newim.storage.mongo;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.IM_MESSAGE;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.REF_GROUP_MEMBER;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.Message.GroupMessage;
import com.acmedcare.framework.newim.Message.SingleMessage;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Mongo Repository Implements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Repository
public class MessageRepositoryImpl implements MessageRepository {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public MessageRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
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
      String groupId, String receiverId, long limit, boolean queryLeast, long leastMessageId) {

    // 用户是否属于该群组
    Query checkQuery =
        new Query(Criteria.where("groupId").is(groupId).and("memberId").is(receiverId));
    boolean result = this.mongoTemplate.exists(checkQuery, REF_GROUP_MEMBER);
    if (!result) {
      mongoLog.warn("用户:{} ,不属于群组:{}", receiverId, groupId);
      return Lists.newArrayList();
    }

    // 默认查询最新的消息列表
    Query messageQuery = new Query(Criteria.where("group").is(groupId));
    messageQuery.with(new Sort(Direction.DESC, "innerTimestamp")).limit((int) limit);

    if (!queryLeast && leastMessageId > 0) {
      try {
        // query least message id innerTimestamp
        Query leastMessageIdQuery = new Query(Criteria.where("mid").is(leastMessageId)).limit(1);
        Message leastMessage =
            this.mongoTemplate.findOne(leastMessageIdQuery, Message.class, IM_MESSAGE);

        if (leastMessage != null) {

          mongoLog.info("查询群组历史消息,客户端最新的消息编号:{}", leastMessage.getMid());
          long innerTimestamp = leastMessage.getInnerTimestamp();
          messageQuery =
              new Query(
                  Criteria.where("group")
                      .is(groupId)
                      .and("innerTimestamp") // 查询的消息的时间小于客户端执行的最新消息的时间
                      .lt(innerTimestamp));
          messageQuery.with(new Sort(Direction.DESC, "innerTimestamp")).limit((int) limit);
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
      String sender, String receiverId, long limit, boolean queryLeast, long leastMessageId) {
    mongoLog.info(
        "查询消息列表,参数:{},{},{},{},{}", sender, receiverId, limit, queryLeast, leastMessageId);

    // 默认查询最新的消息列表
    Query messageQuery =
        new Query(Criteria.where("sender").is(sender).and("receiver").is(receiverId));
    messageQuery.with(new Sort(Direction.DESC, "innerTimestamp")).limit((int) limit);

    if (!queryLeast && leastMessageId > 0) {
      try {
        // query least message id innerTimestamp
        Query leastMessageIdQuery = new Query(Criteria.where("mid").is(leastMessageId)).limit(1);
        Message leastMessage =
            this.mongoTemplate.findOne(leastMessageIdQuery, Message.class, IM_MESSAGE);

        if (leastMessage != null) {

          mongoLog.info("查询历史消息,客户端最新的消息编号:{}", leastMessage.getMid());
          long innerTimestamp = leastMessage.getInnerTimestamp();
          messageQuery =
              new Query(
                  Criteria.where("sender")
                      .is(sender)
                      .and("receiver")
                      .is(receiverId)
                      .and("innerTimestamp") // 查询的消息的时间小于客户端执行的最新消息的时间
                      .lt(innerTimestamp));
          messageQuery.with(new Sort(Direction.DESC, "innerTimestamp")).limit((int) limit);
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
}
