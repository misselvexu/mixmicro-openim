package com.acmedcare.framework.newim.storage.mongo;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.storage.mongo.IMStorageCollections.IM_MESSAGE;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    mongoLog.info("保存操作,消息类型:{} ,内容:{} ", IM_MESSAGE.collectionName(), message.toString());

    // check is saved
    boolean exist =
        mongoTemplate.exists(
            new Query(Criteria.where("mid").is(message.getMid())), IM_MESSAGE.collectionName());

    if (!exist) {
      mongoTemplate.save(message, IM_MESSAGE.collectionName());
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
            new Query(Criteria.where("mid").in(saveMessageIds)),
            "mid",
            IM_MESSAGE.collectionName(),
            Long.class);

    mongoLog.warn("[忽略]批量保存消息,部分消息已经存在,列表:{}", Arrays.toString(existsIds.toArray()));

    List<Message> readySaveMessages = Lists.newArrayList();
    Arrays.stream(messages)
        .forEach(
            message -> {
              if (!existsIds.contains(message.getMid())) {
                readySaveMessages.add(message);
              }
            });

    mongoTemplate.insert(readySaveMessages, IM_MESSAGE.collectionName());
    List<Long> result = Lists.newArrayList();
    readySaveMessages.forEach(message -> result.add(message.getMid()));

    mongoLog.info("批量保存成功的消息编号:{}", Arrays.toString(result.toArray()));

    return result.toArray(new Long[0]);
  }
}
