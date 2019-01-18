package com.acmedcare.framework.newim.master.services;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
      String namespace,
      String groupId,
      String groupName,
      String groupOwner,
      String groupBizTag,
      String groupExt,
      List<Member> members) {
    //
    Group group =
        Group.builder()
            .namespace(namespace)
            .groupId(groupId)
            .groupName(groupName)
            .groupOwner(groupOwner)
            .groupBizTag(groupBizTag)
            .groupExt(groupExt)
            .build();

    this.groupRepository.saveGroup(group);

    if (members != null && !members.isEmpty()) {
      // members
      GroupMembers groupMembers = GroupMembers.builder().groupId(groupId).members(members).namespace(namespace).build();
      this.groupRepository.saveGroupMembers(groupMembers);
    }
  }

  public void addNewGroupMembers(String namespace, String groupId, List<Member> members) {
    this.groupRepository.saveGroupMembers(
        GroupMembers.builder().namespace(namespace).groupId(groupId).members(members).namespace(namespace).build());
  }

  public void removeNewGroupMembers(String namespace, String groupId, List<String> memberIds) {
    this.groupRepository.removeGroupMembers(namespace, groupId, memberIds);
  }

  public Group updateGroup(
      String namespace,
      String groupId,
      String groupName,
      String groupOwner,
      String groupBizTag,
      String groupExt) {

    Group group = this.groupRepository.queryGroup(namespace, groupId);

    if (group == null) {
      throw new StorageException("无效的群组ID");
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

  public Group removeGroup(String namespace, String groupId) {
    Group group = this.groupRepository.queryGroup(namespace, groupId);

    if (group == null) {
      throw new StorageException("无效的群组ID");
    }

    long row = this.groupRepository.removeGroup(namespace, groupId);

    return group;
  }

  public List<Member> queryGroupMemberList(String namespace, String groupId) {
    return this.groupRepository.queryGroupMembersList(namespace, groupId);
  }

  public List<Group> queryGroupList(String namespace, String groupBizType) {
    return this.groupRepository.queryGroupList(namespace, groupBizType);
  }
}
