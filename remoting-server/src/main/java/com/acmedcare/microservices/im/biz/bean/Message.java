package com.acmedcare.microservices.im.biz.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base Message
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {

  private static final long serialVersionUID = 1213375068246340023L;

  /** Message Id */
  private Long mid;

  /** message innerType */
  private InnerType innerType;

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

  /** 单聊消息 */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class SingleMessage extends Message {

    private static final long serialVersionUID = 8573237210255043188L;
    private String receiver;

    @JSONField(serialize = false)
    private boolean readFlag;

    @Builder
    public SingleMessage(
        Long mid,
        InnerType innerType,
        String sender,
        MessageType messageType,
        byte[] body,
        Date sendTimestamp,
        String receiver,
        boolean readFlag) {
      super(mid, innerType, sender, messageType, body, sendTimestamp);
      this.receiver = receiver;
      this.readFlag = readFlag;
    }
  }

  /** 群组消息 */
  @Getter
  @Setter
  @NoArgsConstructor
  public static class GroupMessage extends Message {

    private static final long serialVersionUID = 7000304314077119170L;
    private String group;
    private List<String> receivers;
    /** 未读人数 */
    private int unReadSize;

    @Builder
    public GroupMessage(
        Long mid,
        InnerType innerType,
        String sender,
        MessageType messageType,
        byte[] body,
        Date sendTimestamp,
        String group,
        List<String> receivers) {
      super(mid, innerType, sender, messageType, body, sendTimestamp);
      this.group = group;
      this.receivers = receivers;
    }
  }
}
