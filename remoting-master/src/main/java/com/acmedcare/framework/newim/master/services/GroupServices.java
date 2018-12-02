package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.exception.StorageExecuteException;
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
      String groupId,
      String groupName,
      String groupOwner,
      String groupBizTag,
      String groupExt,
      List<String> memberIds) {
    //
    Group group =
        Group.builder()
            .groupId(groupId)
            .groupName(groupName)
            .groupOwner(groupOwner)
            .groupBizTag(groupBizTag)
            .groupExt(groupExt)
            .build();

    this.groupRepository.saveGroup(group);

    if (memberIds != null && !memberIds.isEmpty()) {
      // members
      GroupMembers members = GroupMembers.builder().groupId(groupId).memberIds(memberIds).build();
      this.groupRepository.saveGroupMembers(members);
    }
  }

  public void addNewGroupMembers(String groupId, List<String> memberIds) {
    this.groupRepository.saveGroupMembers(
        GroupMembers.builder().groupId(groupId).memberIds(memberIds).build());
  }

  public void removeNewGroupMembers(String groupId, List<String> memberIds) {
    this.groupRepository.removeGroupMembers(groupId, memberIds);
  }

  public Group updateGroup(
      String groupId, String groupName, String groupOwner, String groupBizTag, String groupExt) {

    Group group = this.groupRepository.queryGroup(groupId);

    if (group == null) {
      throw new StorageExecuteException("无效的群组ID");
    }

    this.groupRepository.updateGroup(
        Group.builder()
            .groupId(groupId)
            .groupName(groupName)
            .groupOwner(groupOwner)
            .groupBizTag(groupBizTag)
            .groupExt(groupExt)
            .build());

    return group;
  }

  public Group removeGroup(String groupId) {
    Group group = this.groupRepository.queryGroup(groupId);

    if (group == null) {
      throw new StorageExecuteException("无效的群组ID");
    }

    long row = this.groupRepository.removeGroup(groupId);

    return group;
  }
}
