package com.acmedcare.framework.newim.server.service;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.GroupMemberRef;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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

  public List<Group> queryAccountGroups(String namespace, String passportId) {
    return this.groupRepository.queryMemberGroups(namespace, passportId);
  }

  public void joinGroup(String namespace, String groupId, List<Member> members) {
    GroupMembers groupMembers = new GroupMembers(namespace, groupId, members);
    this.groupRepository.saveGroupMembers(groupMembers);
  }

  public void leaveGroup(String namespace, String groupId, List<String> memberIds) {
    this.groupRepository.removeGroupMembers(namespace, groupId, memberIds);
  }

  public List<Member> queryGroupMembers(String namespace, String groupId) {
    List<GroupMemberRef> refs = this.groupRepository.queryGroupMembers(namespace, groupId);
    List<Member> members = Lists.newArrayList();
    for (GroupMemberRef ref : refs) {
      members.add(
          Member.builder()
              .memberId(Long.parseLong(ref.getMemberId()))
              .memberName(ref.getMemberName())
              .memberUserName(ref.getMemberUserName())
              .memberExt(ref.getMemberExt())
              .portrait(ref.getPortrait())
              .build());
    }
    return members;
  }

  public Group queryGroupDetail(String groupId, String namespace) {
    return this.groupRepository.queryGroup(namespace, groupId);
  }
}
