package com.acmedcare.framework.remoting.mq.client.events;

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
   * @return
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
    SDK_INIT,

    RE_CONNECT_FAILED
  }

  /** Biz Event */
  public enum BizEvent implements Event {

    /** 收到主题消息事件 */
    ON_TOPIC_MESSAGE_EVENT
  }

  public interface Event {}
}
