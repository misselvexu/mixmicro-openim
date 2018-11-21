package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.alibaba.fastjson.annotation.JSONField;
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

  private static final long serialVersionUID = 8394184386412740132L;

  @CFNotNull
  @JSONField(name = "passport")
  private String username;

  @CFNotNull private Long passportId;
  @CFNotNull private String areaNo;
  @CFNotNull private String orgId;
  @CFNotNull private String accessToken;
  @CFNotNull private String deviceId;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
