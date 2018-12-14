package com.acmedcare.framework.newim.storage;

import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import com.acmedcare.framework.newim.storage.mongo.GroupRepositoryImpl;
import com.acmedcare.framework.newim.storage.mongo.MessageRepositoryImpl;
import com.acmedcare.framework.newim.storage.mongo.TopicRepositoryImpl;
import com.mongodb.MongoClient;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Mongo Storage Auto Configuration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 22/11/2018.
 */
@Configuration
@ConditionalOnClass({MongoTemplate.class, MongoClient.class})
@AutoConfigureAfter({MongoDataAutoConfiguration.class})
public class MongoStorageAutoConfiguration {

  @Bean
  @Primary
  GroupRepository groupRepository(
      MongoTemplate mongoTemplate,
      MongoTransactionManager mongoTransactionManager,
      MongoClient mongoClient,
      TransactionTemplate transactionTemplate) {
    return new GroupRepositoryImpl(
        mongoTemplate, mongoTransactionManager, mongoClient, transactionTemplate);
  }

  @Bean
  @Primary
  MessageRepository messageRepository(
      MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
    return new MessageRepositoryImpl(mongoTemplate, transactionTemplate);
  }

  @Bean
  @Primary
  TopicRepository topicRepository(
      MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
    return new TopicRepositoryImpl(mongoTemplate, transactionTemplate);
  }

  @Bean
  MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
    return new MongoTransactionManager(dbFactory);
  }
}
