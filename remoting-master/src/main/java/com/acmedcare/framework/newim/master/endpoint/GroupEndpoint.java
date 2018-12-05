package com.acmedcare.framework.newim.master.endpoint;

import static com.acmedcare.framework.newim.MasterLogger.endpointLog;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.ADD_GROUP_MEMBERS;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.CREATE_GROUP;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.REMOVE_GROUP;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.REMOVE_GROUP_MEMBERS;
import static com.acmedcare.framework.newim.client.EndpointConstants.GroupRequest.UPDATE_GROUP;

import com.acmedcare.framework.exception.defined.InvalidRequestParamException;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Group;
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
          request.getGroupId(),
          request.getGroupName(),
          request.getGroupOwner(),
          request.getGroupBizTag(),
          request.getGroupExt(),
          request.getMemberIds());

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
  ResponseEntity removeGroup(@RequestParam String groupId) {
    try {
      endpointLog.info("remove group request params: {}", groupId);

      if (StringUtils.isAnyBlank(groupId)) {
        throw new InvalidRequestParamException("删除群组参数异常");
      }

      Group oldGroup = this.groupServices.removeGroup(groupId);

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

  @PostMapping(ADD_GROUP_MEMBERS)
  ResponseEntity addNewMembers(@RequestBody AddGroupMembersRequest request) {
    try {
      endpointLog.info("add group members request params: {}", JSON.toJSONString(request));

      if (StringUtils.isAnyBlank(request.getGroupId())) {
        throw new InvalidRequestParamException("群组标识ID不能为空");
      }

      if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
        throw new InvalidRequestParamException("添加群组成员列表不能为空");
      }

      this.groupServices.addNewGroupMembers(request.getGroupId(), request.getMemberIds());

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

      this.groupServices.removeNewGroupMembers(request.getGroupId(), request.getMemberIds());

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
