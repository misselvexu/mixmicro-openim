package com.acmedcare.framework.newim.client;

import com.acmedcare.framework.newim.client.bean.request.AddGroupMembersRequest;
import com.acmedcare.framework.newim.client.bean.request.NewGroupRequest;
import com.acmedcare.framework.newim.client.bean.request.RemoveGroupMembersRequest;
import com.acmedcare.framework.newim.client.bean.request.UpdateGroupRequest;
import com.acmedcare.framework.newim.client.bean.response.GroupResponse;
import com.acmedcare.framework.newim.client.exception.EndpointException;
import com.acmedcare.nas.api.ProgressCallback;
import com.acmedcare.nas.api.exception.NasException;
import java.io.File;
import java.security.acl.Group;
import java.util.List;

/**
 * Master Endpoint Service
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
public interface MasterEndpointService {

  /**
   * Create New Group Api
   *
   * @param request create request instance of {@link NewGroupRequest}
   * @throws EndpointException throw failed exception
   */
  void createNewGroup(NewGroupRequest request) throws EndpointException;

  /**
   * Member(s) Join Group Api
   *
   * @param request join group request instance of {@link AddGroupMembersRequest}
   * @throws EndpointException throw failed exception
   */
  void joinGroup(AddGroupMembersRequest request) throws EndpointException;

  /**
   * Remove Group Member(s)
   *
   * @param request remove group member request instance of {@link RemoveGroupMembersRequest}
   * @throws EndpointException throw failed exception
   */
  void removeGroupMembers(RemoveGroupMembersRequest request) throws EndpointException;

  /**
   * Update Group Api
   *
   * @param request update group request instance of {@link UpdateGroupRequest}
   * @return return old {@link Group} instance
   * @throws EndpointException throw failed exception
   */
  GroupResponse updateGroup(UpdateGroupRequest request) throws EndpointException;

  /**
   * Remove Default Namespace Group Api
   *
   * @param groupId group id
   * @return removed {@link Group} instance
   * @throws EndpointException throw failed exception
   */
  GroupResponse removeGroup(String groupId) throws EndpointException;

  /**
   * Remove Namespace Group Api
   *
   * @param groupId group id
   * @param namespace namespace
   * @return removed {@link Group} instance
   * @throws EndpointException throw failed exception
   */
  GroupResponse removeGroup(String groupId, String namespace) throws EndpointException;

  /**
   * Send Single Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receiver message receiver , dest passport id
   * @param content message content
   * @param contentType content type of {@link MessageContentType}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   * @see MessageAttribute
   */
  void sendSingleMessage(
      MessageBizType bizType,
      String sender,
      String receiver,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException;

  /**
   * Send Single Media Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receiver message receiver , dest passport id
   * @param file message content
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @throws NasException nas failed exception
   * @see MessageBizType
   * @see MessageContentType
   * @see MessageAttribute
   */
  void sendSingleMessage(
      MessageBizType bizType,
      String sender,
      String receiver,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException;

  /**
   * Batch Send Single Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receivers message receivers , dest passport ids list
   * @param content message content
   * @param contentType content type of {@link MessageContentType}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  void batchSendSingleMessages(
      MessageBizType bizType,
      String sender,
      List<String> receivers,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException;

  /**
   * Batch Send Single Media Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param receivers message receivers , dest passport ids list
   * @param file message content
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @throws NasException nas failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  void batchSendSingleMessages(
      MessageBizType bizType,
      String sender,
      List<String> receivers,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException;

  /**
   * Send Group Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param groupId group id
   * @param content message content
   * @param contentType content type of {@link MessageContentType}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  void sendGroupMessage(
      MessageBizType bizType,
      String sender,
      String groupId,
      String content,
      MessageContentType contentType,
      MessageAttribute messageAttribute)
      throws EndpointException;

  /**
   * Send Group Message
   *
   * @param bizType bizType of {@link MessageBizType}
   * @param sender message sender ,passport id
   * @param groupId group id
   * @param file message content
   * @param progressCallback progress callback of {@link ProgressCallback}
   * @param messageAttribute message attribute instance of {@link MessageAttribute}
   * @throws EndpointException throw failed exception
   * @throws NasException nas failed exception
   * @see MessageBizType
   * @see MessageContentType
   */
  void sendGroupMessage(
      MessageBizType bizType,
      String sender,
      String groupId,
      File file,
      MessageAttribute messageAttribute,
      ProgressCallback progressCallback)
      throws EndpointException, NasException;
}
