package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Server Push Message Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
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

  @Override
  public void checkFields() throws RemotingCommandException {}
}
