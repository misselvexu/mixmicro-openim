package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.storage.api.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * Group Repository Impl
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Repository
public class GroupRepositoryImpl implements GroupRepository {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public GroupRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }
}
