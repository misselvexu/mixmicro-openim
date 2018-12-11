package com.acmedcare.framework.newim.client.bean.request;

import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.client.MessageConstants;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Base Send Message Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class BaseMessageRequest implements Serializable {

  private static final long serialVersionUID = -4138322119258232048L;

  /** 质量保证 */
  private boolean qos = false;

  /** 发送失败后,最大重试次数 */
  private int maxRetryTimes = MessageConstants.DEFAULT_QOS_MAX_RETRY_TIMES;

  /** 消息重试间隔 */
  private long retryPeriod = MessageConstants.DEFAULT_RETRY_PERIOD;

  /** 是否持久化消息 */
  private boolean persistent = true;

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  /**
   * Get Message Attributes
   *
   * @return {@link MessageAttribute} instance
   */
  public MessageAttribute attribute() {
    return MessageAttribute.builder()
        .maxRetryTimes(maxRetryTimes)
        .persistent(persistent)
        .qos(qos)
        .retryPeriod(retryPeriod)
        .namespace(namespace)
        .build();
  }
}
