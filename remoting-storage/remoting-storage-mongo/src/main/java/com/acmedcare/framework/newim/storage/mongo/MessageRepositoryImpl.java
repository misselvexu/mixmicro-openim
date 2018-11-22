package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(MessageRepositoryImpl.class);
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
    boolean exist =
        mongoTemplate.exists(
            new Query(Criteria.where("mid").is(message.getMid())),
            IMStorageCollections.MESSAGE.collectionName());
    if (!exist) {
      mongoTemplate.save(message, IMStorageCollections.MESSAGE.collectionName());
    } else {
      LOG.warn("[NEW-IM-DB] 消息:{},已经存在,不重复添加", message.getMid());
    }
    return message.getMid();
  }
}
