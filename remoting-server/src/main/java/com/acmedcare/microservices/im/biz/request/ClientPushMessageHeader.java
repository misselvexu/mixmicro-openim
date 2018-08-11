package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.microservices.im.biz.bean.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client Push Message Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientPushMessageHeader extends BaseHeader {

  private String messageType;

  @Builder
  public ClientPushMessageHeader(String username, String messageType) {
    super(username);
    this.messageType = messageType;
  }

  /**
   * Decode Message Type
   *
   * @return type enum
   */
  public Message.MessageType decodeType() {
    return Message.MessageType.valueOf(messageType);
  }
}
