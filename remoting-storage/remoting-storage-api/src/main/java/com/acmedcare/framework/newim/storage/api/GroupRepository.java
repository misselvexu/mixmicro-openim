package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.GroupMemberRef;
import java.util.List;

/**
 * Group Repository Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface GroupRepository {

  Group queryGroup(String namespace, String groupId);

  void saveGroup(Group group);

  long removeGroup(String namespace, String groupId);

  void saveGroupMembers(GroupMembers members);

  long removeGroupMembers(String namespace,String groupId, List<String> memberIds);

  List<String> queryGroupMemberIds(String namespace,String groupId);

  List<GroupMemberRef> queryGroupMembers(String namespace,String groupId);

  List<Group> queryMemberGroups(String namespace,String passportId);

  void updateGroup(Group group);
}
