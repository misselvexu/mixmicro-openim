package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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
  @CFNotNull private String namespace = Constants.DEFAULT_NAMESPACE;
  @CFNotNull private String passport;
  @CFNotNull private String passportId;

  @Builder
  public PullGroupHeader(String passport, String passportId,String namespace) {
    this.passport = passport;
    this.passportId = passportId;
    this.namespace = namespace;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
