package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import java.util.List;

/**
 * Group Repository Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface GroupRepository {

  void saveGroup(Group group);

  long removeGroup(String groupId);

  void saveGroupMembers(GroupMembers members);

  long removeGroupMembers(String groupId, List<String> memberIds);

  List<String> queryGroupMembers(String groupId);
}
