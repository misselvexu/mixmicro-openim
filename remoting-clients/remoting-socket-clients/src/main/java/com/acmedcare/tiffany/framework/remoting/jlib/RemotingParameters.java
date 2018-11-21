package com.acmedcare.tiffany.framework.remoting.jlib;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.request.AuthRequest;
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

  @Getter private String username;

  @Getter private AuthRequest.AuthCallback authCallback;

  @Getter @Default private int reConnectPeriod = 10;

  @Getter @Default private int reConnectRetryTimes = 5;

  @Getter @Default private int heartbeatPeriod = 10;

  @Getter @Default private boolean enableSSL = false;
}
