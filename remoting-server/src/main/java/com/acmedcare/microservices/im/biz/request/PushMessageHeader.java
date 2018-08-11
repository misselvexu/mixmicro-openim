package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Push Message Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PushMessageHeader implements CommandCustomHeader {

  private String messageType;

  @Builder
  public PushMessageHeader(String messageType) {
    this.messageType = messageType;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
