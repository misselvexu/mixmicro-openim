package com.acmedcare.framework.newim.server.mq.service;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Topic;
import com.acmedcare.framework.newim.Topic.TopicSubscribe;
import com.acmedcare.framework.newim.server.master.connector.MasterConnector;
import com.acmedcare.framework.newim.server.mq.MQContext;
import com.acmedcare.framework.newim.server.mq.exception.MQServiceException;
import com.acmedcare.framework.newim.server.mq.processor.body.TopicSubscribeMapping;
import com.acmedcare.framework.newim.server.mq.processor.body.TopicSubscribeMapping.TopicMapping;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import com.alibaba.fastjson.JSON;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MQService Implement
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public class MQService {

  private static final Logger logger = LoggerFactory.getLogger(MQService.class);

  private final TopicRepository topicRepository;
  private final Snowflake snowflake;

  @Autowired private MasterConnector masterConnector;
  private Cache<Long, MQMessage> cache;

  public MQService(TopicRepository topicRepository, Snowflake snowflake) {
    this.topicRepository = topicRepository;
    this.snowflake = snowflake;
    cache =
        CacheBuilder.newBuilder().maximumSize(1024).expireAfterWrite(30, TimeUnit.SECONDS).build();
  }

  /**
   * Create New Topics
   *
   * @param topics topic list
   */
  public Long[] createNewTopic(Topic... topics) {

    logger.info("创建主题: {}", Arrays.toString(topics));
    for (Topic topic : topics) {
      topic.setTopicId(snowflake.nextId());
    }

    this.topicRepository.save(topics);

    Long[] result = new Long[topics.length];
    for (int i = 0; i < topics.length; i++) {
      result[i] = topics[i].getTopicId();
    }

    return result;
  }

  public List<Topic> pullTopicsList(String namespace) {
    return this.topicRepository.queryTopics(namespace);
  }

  public void subscribeTopics(
      String namespace, String passportId, String passport, String[] topicIds) {

    logger.info("请求订阅主题:{},{},{}", namespace, passportId, Arrays.toString(topicIds));
    List<TopicSubscribe> subscribes = Lists.newArrayList();
    if (ArrayUtils.isNotEmpty(topicIds)) {
      for (String topicId : topicIds) {
        subscribes.add(
            TopicSubscribe.builder()
                .namespace(namespace)
                .passportId(Long.parseLong(passportId))
                .topicId(Long.parseLong(topicId))
                .build());
      }

      this.topicRepository.saveSubscribes(subscribes.toArray(new Topic.TopicSubscribe[0]));
    }
  }

  public void unSubscribeTopics(
      String namespace, String passportId, String passport, String[] topicIds) {
    this.topicRepository.removeSubscribes(namespace, passportId, topicIds);
  }

  public TopicSubscribeMapping pullTopicSubscribedMapping(
      String namespace, Long topicId, String passportId, String passport) {

    logger.info("查询主题订阅明细:{},{},{}", namespace, topicId);
    Topic topic = this.topicRepository.queryTopicDetail(namespace, topicId);

    if (topic == null) {
      throw new MQServiceException("无效的主题编号:" + topicId + " ,命名空间:" + namespace);
    }

    List<TopicSubscribe> subscribes = this.topicRepository.queryTopicSubscribes(namespace, topicId);

    TopicSubscribeMapping mapping = new TopicSubscribeMapping();
    if (subscribes != null && !subscribes.isEmpty()) {
      List<TopicMapping> mappings = new ArrayList<>();
      TopicMapping topicMapping = new TopicMapping();
      BeanUtils.copyProperties(topic, topicMapping);
      for (TopicSubscribe subscribe : subscribes) {
        topicMapping.getSubscribeIdsList().add(subscribe.getPassportId().toString());
      }
      mappings.add(topicMapping);
      mapping.setMappings(mappings);
    }

    logger.info("订阅明细:{}", JSON.toJSONString(mapping));

    return mapping;
  }

  public void broadcastTopicMessages(MQContext context, MQMessage mqMessage) {

    logger.info("广播主题消息:{}", mqMessage.toString());
    Long topicId = mqMessage.getTopicId();

    List<TopicSubscribe> subscribes =
        this.topicRepository.queryTopicSubscribes(mqMessage.getNamespace(), topicId);

    try {
      // TODO save cache
      cache.put(mqMessage.getMid(), mqMessage);
    } catch (Exception ignore) {
      logger.warn("[ignore] flush to cache failed.");
    }

    // broadcast
    if (subscribes != null && !subscribes.isEmpty()) {
      logger.info("分发主题[{}]订阅消息到订阅客户端", mqMessage.getTopicId());
      context.broadcastTopicMessages(subscribes, mqMessage);
    }

    // -> master
    if (masterConnector != null) {
      logger.info("转发主题消息到Replica服务器");
      context.broadcastMessage(mqMessage);
    }
  }

  public List<MQMessage> queryMessageList(
      String namespace, Long lastTopicMessageId, int limit, Long topicId) {
    return null;
  }
}
