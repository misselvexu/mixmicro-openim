package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
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
  private static final long serialVersionUID = 5727053760982977774L;

  @CFNotNull private String messageType;
  @CFNotNull private String username;

  @Builder
  public ClientPushMessageHeader(String username, String messageType) {
    this.username = username;
    this.messageType = messageType;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
