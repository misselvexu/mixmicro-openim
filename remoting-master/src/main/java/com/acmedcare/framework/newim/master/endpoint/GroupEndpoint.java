package com.acmedcare.framework.newim.master.endpoint;

import com.acmedcare.framework.exception.defined.InvalidRequestParamException;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.client.bean.request.AddGroupMembersRequest;
import com.acmedcare.framework.newim.client.bean.request.NewGroupRequest;
import com.acmedcare.framework.newim.client.bean.request.RemoveGroupMembersRequest;
import com.acmedcare.framework.newim.client.bean.request.UpdateGroupRequest;
import com.acmedcare.framework.newim.master.services.GroupServices;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.acmedcare.framework.newim.MasterLogger.endpointLog;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.*;

/**
 * Group Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@RestController
public class GroupEndpoint {

  // ========================= Inject Bean Defined =============================

  private final GroupServices groupServices;

  @Autowired
  public GroupEndpoint(GroupServices groupServices) {
    this.groupServices = groupServices;
  }
  // ========================= Request Mapping Method ==========================

  @PostMapping(CREATE_GROUP)
  ResponseEntity createNewGroup(@RequestBody NewGroupRequest request) {
    try {
      endpointLog.info("create group request params: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(
          request.getGroupId(), request.getGroupName(), request.getGroupOwner())) {
        throw new InvalidRequestParamException("创建群组参数异常");
      }

      this.groupServices.createGroup(
          request.getNamespace(),
          request.getGroupId(),
          request.getGroupName(),
          request.getGroupOwner(),
          request.getGroupBizTag(),
          request.getGroupExt(),
          request.getMembers());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("create group failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(UPDATE_GROUP)
  ResponseEntity updateGroup(@RequestBody UpdateGroupRequest request) {
    try {
      endpointLog.info("update group request params: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(
          request.getGroupId(), request.getGroupName(), request.getGroupOwner())) {
        throw new InvalidRequestParamException("更新群组参数异常");
      }

      Group oldGroup =
          this.groupServices.updateGroup(
              request.getNamespace(),
              request.getGroupId(),
              request.getGroupName(),
              request.getGroupOwner(),
              request.getGroupBizTag(),
              request.getGroupExt());

      return ResponseEntity.ok(BizResult.builder().code(0).data(oldGroup).build());
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("update group failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(REMOVE_GROUP)
  ResponseEntity removeGroup(
      @RequestParam String groupId,
      @RequestParam(required = false, defaultValue = MessageConstants.DEFAULT_NAMESPACE)
          String namespace) {
    try {
      endpointLog.info("remove group request params: {}", groupId);

      if (StringUtils.isAnyBlank(groupId)) {
        throw new InvalidRequestParamException("删除群组参数异常");
      }

      Group oldGroup = this.groupServices.removeGroup(namespace, groupId);

      return ResponseEntity.ok(BizResult.builder().code(0).data(oldGroup).build());
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("remove group failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(GROUP_MEMBER_LIST)
  ResponseEntity groupMemberList(
      @RequestParam String groupId,
      @RequestParam(required = false, defaultValue = MessageConstants.DEFAULT_NAMESPACE)
          String namespace) {
    try {
      endpointLog.info("query group member list request params: {}", groupId);

      if (StringUtils.isAnyBlank(groupId)) {
        throw new InvalidRequestParamException("查询群组成员参数异常");
      }

      List<Member> members = this.groupServices.queryGroupMemberList(namespace, groupId);

      return ResponseEntity.ok(members);
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("query group member list failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(GROUP_LIST)
  ResponseEntity groupList(
      @RequestParam String groupBizType,
      @RequestParam(required = false, defaultValue = MessageConstants.DEFAULT_NAMESPACE)
          String namespace) {
    try {
      endpointLog.info("query group list request params: {}", groupBizType);

      if (StringUtils.isAnyBlank(groupBizType)) {
        throw new InvalidRequestParamException("查询群组列表参数异常");
      }

      List<Group> groups = this.groupServices.queryGroupList(namespace, groupBizType);

      if (groups == null || groups.isEmpty()) {
        return ResponseEntity.noContent().build();
      }

      return ResponseEntity.ok(groups);
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("query group list failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(ADD_GROUP_MEMBERS)
  ResponseEntity addNewMembers(@RequestBody AddGroupMembersRequest request) {
    try {
      endpointLog.info("add group members request params: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(request.getGroupId())) {
        throw new InvalidRequestParamException("群组标识ID不能为空");
      }

      if (request.getMembers() == null || request.getMembers().isEmpty()) {
        throw new InvalidRequestParamException("添加群组成员列表不能为空");
      }

      this.groupServices.addNewGroupMembers(
          request.getNamespace(), request.getGroupId(), request.getMembers());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("add group members failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }

  @PostMapping(REMOVE_GROUP_MEMBERS)
  ResponseEntity deleteGroupMembers(@RequestBody RemoveGroupMembersRequest request) {
    try {
      endpointLog.info("remove group members request params: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(request.getGroupId())) {
        throw new InvalidRequestParamException("群组标识ID不能为空");
      }

      this.groupServices.removeNewGroupMembers(
          request.getNamespace(), request.getGroupId(), request.getMemberIds());

      return ResponseEntity.ok().build();
    } catch (InvalidRequestParamException | StorageException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    } catch (Exception e) {
      endpointLog.error("remove group members failed", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder().type(e.getClass()).message(e.getMessage()).build())
                  .build());
    }
  }
}
