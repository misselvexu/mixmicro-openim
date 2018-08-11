package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class BaseHeader implements CommandCustomHeader {

  /** username for client */
  private String username;

  @Override
  public void checkFields() throws RemotingCommandException {}

  public BaseHeader(String username) {
    this.username = username;
  }
}
