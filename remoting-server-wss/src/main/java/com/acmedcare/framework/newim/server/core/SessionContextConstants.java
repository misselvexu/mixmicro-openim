package com.acmedcare.framework.newim.server.core;

import com.acmedcare.framework.aorp.beans.Principal;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

/**
 * Session Context Constants
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 26/11/2018.
 */
public class SessionContextConstants {

  public static final AttributeKey<RemotePrincipal> PRINCIPAL_KEY =
      AttributeKey.newInstance("PASSPORT_PRINCIPAL");

  @Getter
  @Setter
  public static class RemotePrincipal extends Principal {
    private static final long serialVersionUID = -8974853632565363647L;

    private String deviceId;
    private String areaNo;
    private String orgId;
  }
}
