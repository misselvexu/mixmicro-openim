package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;

import lombok.Getter;
import lombok.Setter;

/**
 * Auth Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
public class AuthHeader implements CommandCustomHeader {

  private String username;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
