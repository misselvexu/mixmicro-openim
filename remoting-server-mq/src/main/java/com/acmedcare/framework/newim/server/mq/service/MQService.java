package com.acmedcare.framework.newim.server.mq.service;

import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.newim.Topic;
import com.acmedcare.framework.newim.storage.api.TopicRepository;
import java.util.Arrays;
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
  public void createNewTopic(Topic... topics) {

    logger.info("request to create topics: {}", Arrays.toString(topics));
    for (Topic topic : topics) {
      topic.setTopicId(snowflake.nextId());
    }

    this.topicRepository.save(topics);
  }
}
