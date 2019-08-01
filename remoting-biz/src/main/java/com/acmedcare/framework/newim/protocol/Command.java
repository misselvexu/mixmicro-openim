package com.acmedcare.framework.newim.protocol;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

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

    public static final int CLUSTER_HANDSHAKE = 0x11000;
    /** 通讯服务器注册链接 */
    public static final int CLUSTER_REGISTER = 0x11001;

    /** 通讯服务器停止 */
    public static final int CLUSTER_SHUTDOWN = 0x11002;

    /** 通讯服务器心跳 */
    public static final int CLUSTER_HEARTBEAT = 0x11003;

    /** 通讯服务器之间转发客户端的消息 */
    public static final int CLUSTER_FORWARD_MESSAGE = 0x11004;

    /** 通讯服务器之间转发事件 */
    public static final int CLUSTER_FORWARD_EVENT = 0x11005;
  }

  @Getter
  @Setter
  @Builder
  public static class Retriable {
    @Default private final int maxCounts = 3; // 最大重试次数
    @Default private boolean fastFail = false; // 快速失败
    @Default private boolean retry = false; // 是否开启重试
    @Default private int retryTimes = 0;
    @Default private long period = 5; // 间隔
    @Default private TimeUnit timeUnit = TimeUnit.SECONDS;

    public void retry() {
      retryTimes += 1;
    }
  }

  /**
   * Master Server And Cluster Node Command
   * <li>{@link #CLUSTER_HANDSHAKE}
   * <li>{@link #CLUSTER_PUSH_CLIENT_CHANNELS}
   * <li>{@link #CLUSTER_REGISTER}
   * <li>{@link #CLUSTER_SHUTDOWN}
   * <li>{@link #MASTER_PUSH_MESSAGES}
   */
  public static class MasterClusterCommand {

    /** Cluster Node Register ServerInstance */
    public static final int CLUSTER_REGISTER = 0x20001;

    /** Cluster Node Shutdown Command */
    public static final int CLUSTER_SHUTDOWN = 0x20002;

    /** CLuster Node Heartbeat With Master Servers */
    public static final int CLUSTER_HANDSHAKE = 0x20003;

    /** 通讯节点同步推送本地客户端连接列表 */
    public static final int CLUSTER_PUSH_CLIENT_CHANNELS = 0x20004;

    /** Master服务器推送消息 */
    public static final int MASTER_PUSH_MESSAGES = 0x20005;

    /**
     * WebSocket Client And Cluster Command
     * <li>
     */
    public static final int MASTER_NOTICE_CLIENT_CHANNELS = 0x20006;

    /** cluster 拉取其他的备份节点 */
    public static final int IM_SERVER_PULL_REPLICAS = 0x20007;

    /** cluster 转发信息 */
    public static final int CLUSTER_FORWARD_MESSAGES = 0x20008;
  }

  /** 服务端推送消息到WS 客户端 */
  public static class WebSocketClusterCommand {

    public static final int WS_AUTH = 0x30000;
    public static final int WS_ERROR = -0x3000;
    /** 客户端注册协议 */
    public static final int WS_REGISTER = 0x30001;

    /** 客户端下线协议 */
    public static final int WS_SHUTDOWN = 0x30002;

    /** 客户端心跳协议 */
    public static final int WS_HEARTBEAT = 0x30003;

    /** 推送消息 */
    public static final int WS_PUSH_MESSAGE = 0x30004;

    /** 拉取消息 */
    public static final int WS_PULL_MESSAGE = 0x30005;
  }

  /**
   * Client & IM Server Command
   * <li>{@link #CLIENT_AUTH}
   */
  public static class ClusterClientCommand {

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
     *
     * @since 2.2.0
     */
    public static final int CLIENT_PUSH_MESSAGE_READ_STATUS = 0x40004;

    /**
     * 客户端拉取消息读取详情
     *
     * <p>
     *
     * @since 2.2.0
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
     *
     * @since 2.2.0
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

    /**
     * 客户端收到消息反馈Ack
     *
     * @since 2.2.3
     */
    public static final int CLIENT_RECEIVED_MESSAGE_ACK = 0x41003;
  }
}
