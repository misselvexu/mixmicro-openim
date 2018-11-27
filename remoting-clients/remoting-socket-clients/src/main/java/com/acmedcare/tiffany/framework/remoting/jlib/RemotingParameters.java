package com.acmedcare.tiffany.framework.remoting.jlib;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest;
import com.google.common.base.Strings;
import java.io.File;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;

/**
 * Remoting SDK Params
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
@Builder
public final class RemotingParameters {

  @Getter private ServerAddressHandler serverAddressHandler;

  @Getter private String accessToken;

  @Getter private String username;

  @Getter private Long passportId;

  @Getter private String areaNo;

  @Getter private String orgId;

  @Getter private String deviceId;

  @Getter private AuthRequest.AuthCallback authCallback;

  @Getter @Default private int reConnectPeriod = 10;

  @Getter @Default private int reConnectRetryTimes = 5;

  @Getter @Default private int heartbeatPeriod = 10;

  @Getter @Default private boolean enableSSL = false;

  @Getter private File jksFile;

  @Getter private String jksPassword;

  public boolean validate() {
    if (Strings.isNullOrEmpty(accessToken)) {
      return false;
    }

    boolean and = false;
    if (enableSSL && jksFile != null && jksFile.exists()) {
      and = true;
    }

    return !Strings.isNullOrEmpty(accessToken)
        && !Strings.isNullOrEmpty(username)
        && !Strings.isNullOrEmpty(areaNo)
        && passportId > 0
        && and
        && !Strings.isNullOrEmpty(orgId)
        && !Strings.isNullOrEmpty(deviceId)
        && !Strings.isNullOrEmpty(accessToken)
        && !Strings.isNullOrEmpty(accessToken);
  }
}
