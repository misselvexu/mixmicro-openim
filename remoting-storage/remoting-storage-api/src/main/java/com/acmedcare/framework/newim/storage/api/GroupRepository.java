package com.acmedcare.framework.newim.storage.api;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.GroupMemberRef;
import com.acmedcare.framework.newim.client.bean.Member;

import java.util.List;

/**
 * Group Repository Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface GroupRepository {

  /**
   * Query Group Message
   *
   * @param namespace namespace
   * @param groupId group id
   * @return a instance of {@link Group}
   */
  Group queryGroup(String namespace, String groupId);

  /**
   * Save a new group
   *
   * @param group group instance
   */
  void saveGroup(Group group);

  /**
   * remove group by namespace and group id
   *
   * @param namespace namespace
   * @param groupId group id
   * @return row
   */
  long removeGroup(String namespace, String groupId);

  /**
   * Save group members ref
   *
   * @param members member list
   */
  void saveGroupMembers(GroupMembers members);

  /**
   * remove group members
   *
   * @param namespace namespace
   * @param groupId group id
   * @param memberIds member ids
   * @return removed rows
   */
  long removeGroupMembers(String namespace, String groupId, List<String> memberIds);

  /**
   * query group member id list
   *
   * @param namespace namespace
   * @param groupId group id
   * @return result list
   */
  List<String> queryGroupMemberIds(String namespace, String groupId);

  /**
   * query group member detail
   *
   * @param namespace namespace
   * @param groupId group id
   * @return result list
   */
  List<GroupMemberRef> queryGroupMembers(String namespace, String groupId);

  /**
   * query group member list
   * @param namespace namespace
   * @param groupId group id
   * @return member list
   */
  List<Member> queryGroupMembersList(String namespace, String groupId);

  /**
   * query member groups
   *
   * @param namespace namespace
   * @param passportId passport id
   * @return group list
   */
  List<Group> queryMemberGroups(String namespace, String passportId);

  /**
   * update group message
   *
   * @param group group info
   */
  void updateGroup(Group group);

  /**
   * Query Group List by Biz Tag
   *
   * @param namespace    namespace
   * @param groupBizType group biz type
   * @return group list
   */
  List<Group> queryGroupList(String namespace, String groupBizType);

}
