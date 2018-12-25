package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.Topic;
import com.acmedcare.framework.newim.Topic.TopicSubscribe;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import java.util.List;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Topic Repository Impl
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public class TopicRepositoryImpl implements TopicRepository {

  private final MongoTemplate mongoTemplate;
  private final TransactionTemplate transactionTemplate;

  public TopicRepositoryImpl(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
    this.mongoTemplate = mongoTemplate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public void save(Topic[] topics) {
    // todo
  }

  @Override
  public List<Topic> queryTopics(String namespace) {
    return null;
  }

  @Override
  public void saveSubscribes(TopicSubscribe... subscribes) {}

  @Override
  public void removeSubscribes(String namespace, String passportId, String[] topicIds) {}
}
