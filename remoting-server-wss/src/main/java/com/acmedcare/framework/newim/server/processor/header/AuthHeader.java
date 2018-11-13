package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Auth Request Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public class AuthHeader implements CommandCustomHeader {

  @CFNotNull private String passport;
  @CFNotNull private String passportId;
  @CFNotNull private String accessToken;
  @CFNotNull private String deviceId;

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isAnyBlank(passport, passportId, accessToken, deviceId)) {
      throw new RemotingCommandException("授权用户名,登录授权凭证,设备唯一编号不能为空");
    }
  }
}
