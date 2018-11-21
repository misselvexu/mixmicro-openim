package com.acmedcare.framework.newim.client.bean.request;

import lombok.Getter;
import lombok.Setter;

/**
 * Send Message To Someone
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class SendMessageRequest extends BaseMessageRequest {

  private static final long serialVersionUID = -2972836384487751162L;

  private String sender;
  private String receiver;

  private String content;
  /** 消息类型, Normal,Command */
  private String type;
}
