package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client Push Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class ClientPushMessageHeader extends BaseHeader implements CommandCustomHeader {

  /** passport for client */
  @CFNotNull private String username;

  @CFNotNull private String messageType;

  @Builder
  public ClientPushMessageHeader(String username, String messageType) {
    this.username = username;
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

  @Override
  public void checkFields() throws RemotingCommandException {}
}
