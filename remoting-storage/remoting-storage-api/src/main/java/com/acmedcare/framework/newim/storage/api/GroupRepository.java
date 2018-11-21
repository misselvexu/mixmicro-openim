package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;

/**
 * Group Repository Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface GroupRepository {

  void saveGroup(Group group);

  void saveGroupMembers(GroupMembers members);
}
