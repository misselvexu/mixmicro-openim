package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * Pull Group Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
public class PullGroupHeader implements CommandCustomHeader {

  /** passport for client */
  @CFNotNull private String username;
  @CFNotNull private String passportId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
