package com.acmedcare.framework.newim.client.bean.request;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Batch Send Message To Someone
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class BatchSendMessageRequest extends BaseMessageRequest {

  private static final long serialVersionUID = -2972836384487751162L;

  private String sender;
  private List<String> receivers;

  private String content;
  /** 消息类型, Normal,Command */
  private String type;
}
