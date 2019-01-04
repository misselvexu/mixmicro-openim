package com.acmedcare.framework.remoting.mq.client.biz.bean;

import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import java.util.Date;
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
  private Long topicId;

  /** 主题标识 */
  private String topicTag;

  private String topicName;

  private String topicType;

  /** 消息编号 */
  private Long mid;

  private Long sender;

  /** Message body */
  private byte[] body;

  /** Send Timestamp */
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date sendTimestamp = new Date();
}
