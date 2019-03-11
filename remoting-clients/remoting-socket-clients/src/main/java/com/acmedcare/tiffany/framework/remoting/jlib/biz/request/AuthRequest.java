package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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
  /**
   * device type
   *
   * @since 2.2.3
   */
  private String deviceType = "DEFAULT";

  private String namespace = Constants.DEFAULT_NAMESPACE;

  @Builder
  public AuthRequest(
      String username,
      Long passportId,
      String areaNo,
      String orgId,
      String accessToken,
      String deviceId,
      String deviceType,
      String namespace) {
    this.username = username;
    this.passportId = passportId;
    this.areaNo = areaNo;
    this.orgId = orgId;
    this.accessToken = accessToken;
    this.deviceId = deviceId;
    this.deviceType = deviceType;
    this.namespace = namespace;
  }

  /** AuthCallback */
  public interface AuthCallback {

    /** auth succeed. */
    void onSuccess();

    /**
     * Auth Failed ,
     *
     * <p><b><font style='color:red;'>then client will close remoting connection.</font></b>
     *
     * @param code error code
     * @param message error message
     */
    void onFailed(int code, String message);
  }
}
