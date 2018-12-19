package com.acmedcare.framework.remoting.mq.client.biz.bean;

import java.io.Serializable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Message
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
@Getter
@Setter
@NoArgsConstructor
public class Message implements Serializable {

  private static final long serialVersionUID = -3293692113307260482L;

  /** 主题信息 */
  private Topic topic;

  /** 消息编号 */
  private Long messageId;

  /** 消息体信息 */
  private byte[] messageBody;
}
