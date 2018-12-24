package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Topic;

/**
 * Topic Repository
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-14.
 */
public interface TopicRepository {

  /**
   * Save new topics
   *
   * @param topics topics instance
   * @see Topic
   */
  void save(Topic[] topics);
}
