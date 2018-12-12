package com.acmedcare.framework.remoting.mq.client;

import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.nas.client.NasProperties;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.utils.RemotingLogger;
import com.google.common.base.Strings;
import java.io.File;
import java.nio.file.Paths;
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

  private static final String DEFAULT_JKS_PD = "1qaz2wsx";

  @Getter private ServerAddressHandler serverAddressHandler;

  @Getter private NasProperties nasProperties;

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

  @Getter @Default private String jksPassword = DEFAULT_JKS_PD;

  public boolean validate() {

    if (enableSSL) {
      if (jksFile == null || !jksFile.exists()) {
        try {
          // load default
          this.jksFile =
              Paths.get(RemotingCommand.class.getResource("/META-INF/keystore.jks").toURI())
                  .toFile();
          this.jksPassword = DEFAULT_JKS_PD;
        } catch (Exception e) {
          RemotingLogger.warn(null, "load default jks failed.(ignore)");
        }
      }
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
