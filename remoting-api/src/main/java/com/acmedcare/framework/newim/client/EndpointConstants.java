package com.acmedcare.framework.newim.client;

/**
 * {@link EndpointConstants}
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
public interface EndpointConstants {

  /**
   * Group Endpoint Request
   *
   * <p>
   *
   * @since 2.1.0-RC1
   */
  interface GroupRequest {

    /** 创建群组 */
    String CREATE_GROUP = "/group/create-new-group";

    /** 查询群详情 */
    String GROUP_INFO = "/group/detail";

    /** 删除群组 */
    String REMOVE_GROUP = "/group/remove-group";

    /** 更新群组信息 */
    String UPDATE_GROUP = "/group/update-group";

    /** 群组添加人员 */
    String ADD_GROUP_MEMBERS = "/group/add-group-members";

    /** 群组列表 */
    String GROUP_LIST = "/group/list";

    /** 群组移除人员 */
    String REMOVE_GROUP_MEMBERS = "/group/remove-group-members";

    /** 群组成员列 */
    String GROUP_MEMBER_LIST = "/group/members-list";
  }

  /**
   * Message Endpoint Request
   *
   * @since 2.1.0-RC1
   */
  interface MessageRequest {

    /** 发送消息 */
    String SEND_MESSAGE = "/message/send-message";

    /** 批量发送消息 */
    String BATCH_SEND_MESSAGE = "/message/batch-send-message";

    /** 发送群组消息 */
    String SEND_GROUP_MESSAGE = "/message/send-group-message";
  }

  interface PushRequest {

    /** 推送通知 */
    String PUSH_NOTICE = "/push/notice";
  }
}
