package com.acmedcare.framework.remoting.mq.client.biz.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Base Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class BaseRequest {

  private String passport;

  private String passportId;

  public BaseRequest(String passport) {
    this.passport = passport;
  }

  public BaseRequest(String passport, String passportId) {
    this.passport = passport;
    this.passportId = passportId;
  }
}
