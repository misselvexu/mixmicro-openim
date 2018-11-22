package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.storage.IMStorageCollections;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Base Message
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

  public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

  private static final long serialVersionUID = 1213375068246340023L;
  /** Message Id */
  @Indexed(unique = true)
  private Long mid;

  /** message innerType */
  private InnerType innerType = InnerType.NORMAL;

  /** message sender */
  private String sender;

  /** 接收人类型 */
  private MessageType messageType = MessageType.GROUP;

  /** Message body */
  private byte[] body;

  /** Send Timestamp */
  @JSONField(format = "yyyy-MM-dd HH:mm:ss")
  private Date sendTimestamp;

  public byte[] bytes() {
    return JSON.toJSONBytes(this);
  }

  @Override
  public String toString() {
    return JSON.toJSONString(this);
  }

  /** 消息类型 */
  public enum InnerType {
    /** 普通消息 */
    NORMAL,

    /** 指令消息 */
    COMMAND
  }

  /** 消息本身的类型 */
  public enum MessageType {
    /** 单人接 */
    SINGLE,
    /** 发群组消息 */
    GROUP
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class QosMessage extends Message {

    private static final long serialVersionUID = -7983416922999708268L;

    /** 是否开启QOS,默认值为 false */
    private boolean qos = false;

    /** 最大重试次数 */
    private int maxRetryTimes = 1;

    /** 重试间隔 */
    private long retryPeriod = 5000;
  }

  /** 单聊消息 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Document(value = IMStorageCollections.IM_MESSAGE)
  public static class SingleMessage extends QosMessage {

    private static final long serialVersionUID = 8573237210255043188L;
    private String receiver;

    private boolean readFlag;
  }

  /** 群组消息 */
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Document(value = IMStorageCollections.IM_MESSAGE)
  public static class GroupMessage extends QosMessage {

    private static final long serialVersionUID = 7000304314077119170L;
    private String group;
    private List<String> receivers;
    /** 未读人数 */
    private int unReadSize;
  }
}
