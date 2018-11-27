package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.MessageAttribute;
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

  /** Default Attribute */
  private MessageAttribute attribute = MessageAttribute.builder().build();

  public BaseRequest(String passport) {
    this.passport = passport;
  }

  public BaseRequest(String passport, String passportId) {
    this.passport = passport;
    this.passportId = passportId;
  }

  public BaseRequest(String passport, String passportId, MessageAttribute attribute) {
    this.passport = passport;
    this.passportId = passportId;
    this.attribute = attribute;
  }
}
