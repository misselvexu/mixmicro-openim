package com.acmedcare.framework.newim.protocol;

/**
 * Command Protocol
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 08/11/2018.
 */
public final class Command {

  /**
   * Cluster Server With Cluster Command
   * <li>{@link #CLUSTER_REGISTER}
   * <li>{@link #CLUSTER_SHUTDOWN}
   * <li>{@link #CLUSTER_HEARTBEAT}
   * <li>{@link #CLUSTER_FORWARD_MESSAGE}
   */
  public static class ClusterWithClusterCommand {

    /** 通讯服务器注册链接 */
    public static final int CLUSTER_REGISTER = 0x11001;

    /** 通讯服务器停止 */
    public static final int CLUSTER_SHUTDOWN = 0x11002;

    /** 通讯服务器心跳 */
    public static final int CLUSTER_HEARTBEAT = 0x11003;

    /** 通讯服务器之间转发客户端的消息 */
    public static final int CLUSTER_FORWARD_MESSAGE = 0x11004;
  }

  /**
   * Master Server And Cluster Node Command
   * <li>{@link #CLUSTER_SHAKEHAND}
   * <li>{@link #CLUSTER_PUSH_CLIENT_CHANNELS}
   * <li>{@link #CLUSTER_REGISTER}
   * <li>{@link #CLUSTER_SHUTDOWN}
   * <li>{@link #MASTER_PUSH_SYSTEM_MESSAGES}
   */
  public static class MasterClusterCommand {

    /** Cluster Node Register ServerInstance */
    public static final int CLUSTER_REGISTER = 0x20001;

    /** Cluster Node Shutdown Command */
    public static final int CLUSTER_SHUTDOWN = 0x20002;

    /** CLuster Node Heartbeat With Master Servers */
    public static final int CLUSTER_SHAKEHAND = 0x20003;

    /** 通讯节点同步推送本地客户端连接列表 */
    public static final int CLUSTER_PUSH_CLIENT_CHANNELS = 0x20004;

    /** Master服务器推送系统消息 */
    public static final int MASTER_PUSH_SYSTEM_MESSAGES = 0x20005;
    /**
     * WebSocket Client And Cluster Command
     * <li>
     */
    public static final int MASTER_NOTICE_CLIENT_CHANNELS = 0x20006;
  }

  public static class WebSocketClusterCommand {

    /** 客户端注册协议 */
    public static final int WS_REGISTER = 0x30001;

    /** 客户端下线协议 */
    public static final int WS_SHUTDOWN = 0x30002;

    /** 客户端心跳协议 */
    public static final int WS_HEARTBEAT = 0x30003;
  }

  /**
   * Client & IM Server Command
   * <li>{@link #CLIENT_AUTH}
   */
  public static class ClusterClientCommand {

    /** 客户端连接授权操作请求 */
    public static final int CLIENT_AUTH = 0x40001;

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
     * 客户端拉取会话列表
     *
     * <pre>
     *
     *
     * </pre>
     */
    public static final int CLIENT_PULL_OWNER_SESSIONS = 0x40003;

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
     * 客户端拉取会话状态(未读数/最后一条消息)
     *
     * <pre>
     *
     * </pre>
     */
    public static final int CLIENT_PULL_SESSION_STATUS = 0x40005;

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
}
