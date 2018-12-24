package com.acmedcare.framework.newim.server.mq.service;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Topic;
import com.acmedcare.framework.newim.server.mq.processor.body.TopicSubscribeMapping;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public MQService(TopicRepository topicRepository, Snowflake snowflake) {
    this.topicRepository = topicRepository;
    this.snowflake = snowflake;
  }

  /**
   * Create New Topics
   *
   * @param topics topic list
   */
  public Long[] createNewTopic(Topic... topics) {

    logger.info("request to create topics: {}", Arrays.toString(topics));
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

  public List<Topic> pullTopicsList() {
    return null;
  }

  public void subscribeTopics(String passportId, String passport, String[] topicIds) {}

  public void ubSubscribeTopics(String passportId, String passport, String[] topicIds) {}

  public List<TopicSubscribeMapping> pullTopicSubscribedMapping(
      String passportId, String passport) {
    return null;
  }

  public void broadcastTopicMessages(MQMessage mqMessage) {

  }

  public List<MQMessage> queryMessageList(Long lastTopicMessageId, int limit, Long topicId) {
    return null;
  }
}
