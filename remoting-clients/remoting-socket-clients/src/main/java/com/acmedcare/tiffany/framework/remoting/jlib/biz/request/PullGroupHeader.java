package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Group Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullGroupHeader extends BaseHeader implements CommandCustomHeader {

  @CFNotNull private String passport;
  @CFNotNull private String passportId;

  @Builder
  public PullGroupHeader(String passport, String passportId) {
    this.passport = passport;
    this.passportId = passportId;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
