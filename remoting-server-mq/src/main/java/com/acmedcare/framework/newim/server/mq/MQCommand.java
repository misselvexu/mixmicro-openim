package com.acmedcare.framework.newim.server.mq;

/**
 * MQ Command Defined Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class MQCommand {

  /**
   * 采样段数据协议
   *
   * @deprecated use {@link ProducerClient} instead of
   */
  public interface SamplingClient {

    /** 握手协议 */
    int HANDSHAKE = 0x50001;

    /** 注册协议(上线) */
    int REGISTER = 0x50002;

    /** 注销协议(下线) */
    int SHUTDOWN = 0x50003;

    /** 拉取订阅主题列表 */
    int PULL_TOPIC_SUBSCRIBE_MAPPING = 0x50004;

    /** 发送主题消息 */
    int SEND_TOPIC_MESSAGE = 0x50005;

    /** 主题删除 */
    int ON_TOPIC_SUBSCRIBED_EMPTY_EVENT = 0x50006;

    /** 取消订阅 */
    int ON_TOPIC_UNSUBSCRIBE_EVENT = 0x50007;
  }

  public interface ProducerClient extends SamplingClient {}

  /**
   * 监护端数据协议
   *
   * @deprecated use {@link ConsumerClient} instead of
   */
  public interface MonitorClient {

    /** 握手协议 */
    int HANDSHAKE = 0x60001;

    /** 注册协议(上线) */
    int REGISTER = 0x60002;

    /** 注销协议(下线) */
    int SHUTDOWN = 0x60003;

    /** 主题订阅协议 */
    int TOPIC_SUBSCRIBE = 0x60004;

    /** 撤销主题订阅协议 */
    int REVOKE_TOPIC_SUBSCRIBE = 0x60005;

    /** 填补数据协议 */
    int FIX_MESSAGE = 0x60006;
  }

  public interface ConsumerClient extends MonitorClient {}

  public interface Common {

    /** 队列服务器推送消息请求 */
    int TOPIC_MESSAGE_PUSH = 0x70001;

    /** 创建主题请求 */
    int CREATE_TOPIC = 0x70002;

    /**
     * 批量创建主题
     *
     * @since 2.2.3
     */
    int CREATE_TOPICS = 0x70012;

    /**
     * 查询主题详情
     *
     * @since 2.2.3
     */
    int QUERY_TOPIC_DETAIL = 0x70013;

    /** 拉取主题列表 */
    int PULL_TOPICS = 0x70003;

    /**
     * 删除主题
     *
     * @since 2.2.3
     */
    int REMOVE_TOPIC = 0x70004;

    /**
     * 主题删除事件通知
     *
     * @since 2.2.3
     */
    int ON_TOPIC_REMOVED_EVENT = 0x70005;
  }
}
