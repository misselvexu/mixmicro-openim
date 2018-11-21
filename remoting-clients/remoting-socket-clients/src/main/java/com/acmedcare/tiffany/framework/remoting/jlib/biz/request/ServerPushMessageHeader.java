package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * com.acmedcare.microservices.im.biz.request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 11/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class ServerPushMessageHeader implements CommandCustomHeader {

  @CFNotNull private String messageType;

  @Builder
  public ServerPushMessageHeader(String messageType) {
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
