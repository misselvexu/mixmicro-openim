package com.acmedcare.framework.newim.client;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息设置
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
@Builder
public class MessageAttribute {

  /** Message Namespace Defined */
  @Default private String namespace = MessageConstants.DEFAULT_NAMESPACE;
  /** 质量保证 */
  @Default private boolean qos = false;

  /** 发送失败后,最大重试次数 */
  @Default private int maxRetryTimes = MessageConstants.DEFAULT_QOS_MAX_RETRY_TIMES;

  /** 消息重试间隔 */
  @Default private long retryPeriod = MessageConstants.DEFAULT_RETRY_PERIOD;

  /** 是否持久化消息 */
  @Default private boolean persistent = true;
}
