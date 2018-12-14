package com.acmedcare.framework.newim.server.mq.service;

import com.acmedcare.framework.newim.storage.api.TopicRepository;

/**
 * MQService Implement
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public class MQService {

  private final TopicRepository topicRepository;

  public MQService(TopicRepository topicRepository) {
    this.topicRepository = topicRepository;
  }
}
