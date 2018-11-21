package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Auth Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class AuthRequest {

  private String username;
  private Long passportId;
  private String areaNo;
  private String orgId;
  private String accessToken;
  private String deviceId;

  @Builder
  public AuthRequest(
      String username,
      Long passportId,
      String areaNo,
      String orgId,
      String accessToken,
      String deviceId) {
    this.username = username;
    this.passportId = passportId;
    this.areaNo = areaNo;
    this.orgId = orgId;
    this.accessToken = accessToken;
    this.deviceId = deviceId;
  }

  /** AuthCallback */
  public interface AuthCallback {
    void onSuccess();

    void onFailed(int code, String message);
  }
}
