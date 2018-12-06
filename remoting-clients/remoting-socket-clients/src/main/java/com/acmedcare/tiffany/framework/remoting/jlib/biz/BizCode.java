package com.acmedcare.tiffany.framework.remoting.jlib.biz;

/**
 * Biz Code Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 02/07/2018.
 */
public class BizCode {

  /** Default Processor Code */
  public static final int PONG = 0x0000;

  /**
   * Heartbeat Processor Code
   *
   * <pre>
   *
   *  Use {@link com.acmedcare.tiffany.framework.remoting.android.core.protocol.RequestCode#SYSTEM_HEARTBEAT_CODE} instead of.
   *
   *  </pre>
   */
  @Deprecated public static final int HEARTBEAT = 0x1001;

  /** 客户端捂手操作 */
  public static final int CLIENT_HANDSHAKE = -0x40000;

  /** 客户端连接授权操作请求 */
  public static final int CLIENT_AUTH = 0x40000;

  /**
   * 客户端拉取[离线]消息
   *
   * <pre>
   *  <li>客户端离线重新上线后,根据本地的群组标识和最后的消息的编号,时间戳拉取 最新的消息
   *
   *  <li>客户端拉取未读
   *
   *  <li>客户端拉取全部消息
   *
   * </pre>
   */
  public static final int CLIENT_PULL_MESSAGE = 0x40001;

  /**
   * 客户端拉取群组列表
   *
   * <pre>
   *
   *
   *
   * </pre>
   */
  public static final int CLIENT_PULL_OWNER_GROUPS = 0x40002;

  /**
   * 客户端推送消息已读状态
   *
   * <pre>
   *
   *
   * </pre>
   */
  public static final int CLIENT_PUSH_MESSAGE_READ_STATUS = 0x40004;

  /**
   * 客户端拉取消息读取详情
   *
   * <p>
   */
  public static final int CLIENT_PULL_GROUP_MESSAGE_READ_STATUS = 0x40005;

  /**
   * 客户端发消息
   *
   * <pre>
   *
   *
   * </pre>
   */
  public static final int CLIENT_PUSH_MESSAGE = 0x40006;

  /**
   * 客户端加群操作
   *
   * <p>
   */
  public static final int CLIENT_JOIN_GROUP = 0x40007;

  /**
   * 客户端退群操作
   *
   * <p>
   */
  public static final int CLIENT_QUIT_GROUP = 0x40008;

  /**
   * 客户端拉取群组人员在线状态
   *
   * <p>
   */
  public static final int CLIENT_PULL_GROUP_MEMBERS_ONLINE_STATUS = 0x40009;

  /**
   * 客户端拉取群组成员列表
   *
   * @since 2.2.0
   */
  public static final int CLIENT_PULL_GROUP_MEMBERS = 0x40010;

  /**
   * 服务端推送消息
   *
   * <pre>
   *
   * </pre>
   */
  public static final int SERVER_PUSH_MESSAGE = 0x41001;

  /**
   * 服务端通知客户端下线通知
   *
   * <pre>
   *
   * </pre>
   */
  public static final int SERVER_PUSH_FOCUS_LOGOUT = 0x41002;
}
