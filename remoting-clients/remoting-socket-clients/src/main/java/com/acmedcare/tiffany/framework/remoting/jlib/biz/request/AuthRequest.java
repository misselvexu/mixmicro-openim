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

  @Builder
  public AuthRequest(String username) {
    this.username = username;
  }

  /** AuthCallback */
  public interface AuthCallback {
    void onSuccess();

    void onFailed(int code, String message);
  }
}
