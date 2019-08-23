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

  /** 账号 */
  String ACCOUNT = "im_account";

  /** 主题 */
  String TOPIC = "mq_topic";

  /** 主题订阅 */
  String TOPIC_SUBSCRIBE = "mq_topic_subscribe";

  /** 群组成员管理 */
  String REF_GROUP_MEMBER = "im_refs_group_member";

  /** 消息已读状态记录表 */
  String MESSAGE_READ_STATUS = "im_message_read_status";

  /** 消息qos状态记录 */
  String MESSAGE_QOS = "im_message_qos_record";

  /** 投递消息记录表 */
  String DELIVERER_MESSAGE = "im_deliverer_message_record";
}
