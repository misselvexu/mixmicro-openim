package com.acmedcare.framework.newim.storage;

/**
 * IM Storage Collections
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
public interface IMStorageCollections {

  /** 消息 */
  String IM_MESSAGE = "im_message";

  /** 队列消息 */
  String MQ_MESSAGE = "mq_message";

  /** 群组 */
  String GROUP = "im_group";

  /** 主题 */
  String TOPIC = "mq_topic";

  /** 群组成员管理 */
  String REF_GROUP_MEMBER = "im_refs_group_member";

  /** 消息已读状态记录表 */
  String MESSAGE_READ_STATUS = "im_message_read_status";
}
