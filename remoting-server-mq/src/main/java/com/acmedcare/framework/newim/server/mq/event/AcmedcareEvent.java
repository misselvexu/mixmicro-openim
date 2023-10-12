package com.acmedcare.framework.newim.server.mq.event;

import com.alibaba.fastjson.JSON;
import java.io.Serializable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Acmedcare Event(s)
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 30/07/2018.
 */
@SuppressWarnings("unused")
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
   * @return event
   */
  public abstract Event eventType();

  /**
   * POST Event With Data or Not , It's is nullable.
   *
   * @return post data with event bus
   */
  public abstract byte[] data();

  /** System Event */
  public enum SystemEvent implements Event {}

  /** Biz Event */
  public enum BizEvent implements Event {

    /** 取消订阅的通知时间 */
    ON_TOPIC_UB_SUBSCRIBE_EVENT,

    /** 主题空订阅事件 */
    ON_TOPIC_EMPTY_SUBSCRIBED_EVENT,

    /** 主题删除事件 */
    ON_TOPIC_REMOVED_EVENT
  }

  public interface Event {}

  @Getter
  @Setter
  @NoArgsConstructor
  public static class OnTopicUnSubscribeEventData implements Serializable {

    private static final long serialVersionUID = -3431779818806156320L;

    private Long passportId;
    private String[] topicIds;

    @Builder
    public OnTopicUnSubscribeEventData(Long passportId, String[] topicIds) {
      this.passportId = passportId;
      this.topicIds = topicIds;
    }

    @Override
    public String toString() {
      return JSON.toJSONString(this);
    }
  }

  @Getter
  @Setter
  @NoArgsConstructor
  public static class OnTopicSubscribeEmptyEventData implements Serializable {

    private static final long serialVersionUID = 4260024650784149548L;
    private String topicId;

    @Builder
    public OnTopicSubscribeEmptyEventData(String topicId) {
      this.topicId = topicId;
    }

    @Override
    public String toString() {
      return JSON.toJSONString(this);
    }
  }
}
