package com.acmedcare.framework.newim.server.service;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Group Service
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
@Component
public class GroupService {

  private final GroupRepository groupRepository;

  @Autowired
  public GroupService(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public List<Group> queryAccountGroups(String passportId) {
    return this.groupRepository.queryMemberGroups(passportId);
  }
}
