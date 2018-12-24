package com.acmedcare.framework.remoting.mq.client.biz;

/**
 * Biz Code Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 02/07/2018.
 */
public class BizCode {

  /** Default Processor Code */
  public static final int PONG = 0x0000;

  /** 客户端连接授权操作请求 */
  public static final int CLIENT_AUTH = 0x40000;

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

    int SEND_TOPIC_MESSAGE = 0x50005;
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

  public interface Common {

    /** 队列服务器推送消息请求 */
    int TOPIC_MESSAGE_PUSH = 0x70001;

    /** 创建主题请求 */
    int CREATE_TOPIC = 0x70002;

    /** 拉取主题列表 */
    int PULL_TOPICS = 0x70003;
  }
}
