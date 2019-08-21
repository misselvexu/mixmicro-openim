package com.acmedcare.tiffany.framework.remoting.jlib.events;

/**
 * Acmedcare Event(s)
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 30/07/2018.
 */
public abstract class AcmedcareEvent {

  /**
   * Event Type
   *
   * <p>System Event
   *
   * <ul>
   *   <li>SDK_INIT - Invoked After SDK init-ed with Client
   * </ul>
   *
   * <p>Biz Event
   *
   * <ul>
   *   <li>CLIENT_AUTH - After SDK Init-ed & Connection is established ! Auth Current Client Token
   *       Validation!
   * </ul>
   *
   * @return instance of {@link Event}
   */
  public abstract Event eventType();

  /**
   * POST Event With Data or Not , It's is nullable.
   *
   * @return post data with event bus
   */
  public abstract Object data();

  /** System Event */
  public enum SystemEvent implements Event {

    /** 初始化完成事件 */
    SDK_INIT,

    RE_CONNECT_FAILED,

    /** 其他设备登录了改账号(服务端延迟3秒关闭连接) */
    LOGIN_ON_OTHER_DEVICE
  }

  /** Biz Event */
  public enum BizEvent implements Event {

    // 拉取信息的返回值事件
    PULL_MESSAGE_RESPONSE,

    // 推送消息已读状态返回事件
    PUSH_MESSAGE_READ_STATUS_RESPONSE,

    /**
     * 拉取会话列表
     *
     * @deprecated 3.0版本中将删除该事件
     */
    PULL_SESSION_LIST_RESPONSE,

    /**
     * 拉取具体的会话的状态信息
     *
     * @deprecated 3.0版本中将删除该事件
     */
    PULL_SESSION_STATUS_RESPONSE,

    // 拉取群组列表返回值事件
    PULL_GROUPS_LIST_RESPONSE,

    // 发送消息回执事件
    PUSH_MESSAGE_RESPONSE,

    // 服务端推送消息事件
    SERVER_PUSH_MESSAGE,

    /**
     * 服务端消息通知
     *
     * @since 2.3.0
     */
    SERVER_NOTIFY_MESSAGE
  }

  public interface Event {}
}
