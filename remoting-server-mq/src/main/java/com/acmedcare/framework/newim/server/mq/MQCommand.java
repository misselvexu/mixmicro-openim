package com.acmedcare.framework.newim.server.mq;

/**
 * MQ Command Defined Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class MQCommand {

  /** 采样段数据协议 */
  public interface SamplingClient {

    /** 握手协议 */
    int HANDSHAKE = 0x50001;

    /** 注册协议(上线) */
    int REGISTER = 0x50002;

    /** 注销协议(下线) */
    int SHUTDOWN = 0x50003;

    /** 拉取订阅主题列表 */
    int PULL_TOPIC_SUBSCRIBE_MAPPING = 0x50004;
  }

  /** 监护端数据协议 */
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

  public interface BroadcastCommand {

    /** 队列服务器推送消息请求 */
    int TOPIC_MESSAGE_PUSH = 0x70001;

    /** 创建主题请求 */
    int CREATE_TOPIC = 0x70002;

    /** 拉取主题列表 */
    int PULL_TOPICS = 0x70003;
  }
}
