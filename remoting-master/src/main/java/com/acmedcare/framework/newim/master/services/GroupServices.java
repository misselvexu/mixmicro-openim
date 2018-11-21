package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Group Services
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Component
public class GroupServices {

  private final GroupRepository groupRepository;

  @Autowired
  public GroupServices(GroupRepository groupRepository) {
    this.groupRepository = groupRepository;
  }

  public void createGroup(
      String groupId, String groupName, String groupOwner, List<String> memberIds) {
    //
    Group group =
        Group.builder().groupId(groupId).groupName(groupName).groupOwner(groupOwner).build();

    // members
    GroupMembers members = GroupMembers.builder().groupId(groupId).memberIds(memberIds).build();

    this.groupRepository.saveGroup(group);
    this.groupRepository.saveGroupMembers(members);
  }

  public void addNewGroupMembers(String groupId, List<String> memberIds) {
    //
  }

  public void removeNewGroupMembers(String groupId, List<String> memberIds) {
    //
  }
}
