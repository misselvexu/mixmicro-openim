package com.acmedcare.microservices.im.biz;

/**
 * Biz Code Defined
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 02/07/2018.
 */
public class BizCode {

  /** Default Processor Code */
  public static final int PONG = 0x0000;

  /** Heartbeat Processor Code */
  public static final int HEARTBEAT = 0x1001;

  /** Auth First */
  public static final int AUTH = 0x2001;

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
  public static final int CLIENT_PULL_MESSAGE = 0x3001;

  /**
   * 客户端拉取群组列表
   *
   * <pre>
   *
   *
   *
   * </pre>
   */
  public static final int CLIENT_PULL_OWNER_GROUPS = 0x3002;

  /**
   * 客户端拉取会话列表
   *
   * <pre>
   *
   *
   * </pre>
   */
  public static final int CLIENT_PULL_OWNER_SESSIONS = 0x3003;

  /**
   * 客户端推送消息已读状态
   *
   * <pre>
   *
   *
   * </pre>
   */
  public static final int CLIENT_PUSH_MESSAGE_READ_STATUS = 0x3004;

  /**
   * 客户端拉取会话状态(未读数/最后一条消息)
   *
   * <pre>
   *
   * </pre>
   */
  public static final int CLIENT_PULL_SESSION_STATUS = 0x3005;


  /**
   * 客户端发消息
   *
   * <pre>
   *
   *
   * </pre>
   *
   */
  public static final int CLIENT_PUSH_MESSAGE = 0x3006;

  /**
   * 服务端推送消息
   *
   * <pre>
   *
   * </pre>
   */
  public static final int SERVER_PUSH_MESSAGE = 0x4001;
}
