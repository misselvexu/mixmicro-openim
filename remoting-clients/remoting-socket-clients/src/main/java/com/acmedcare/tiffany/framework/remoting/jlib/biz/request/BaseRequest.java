package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.Alias;
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

  @Alias("passport")
  private String username;

  private String passportId;

  /** Default Attribute */
  private MessageAttribute attribute = MessageAttribute.builder().build();

  public BaseRequest(String username) {
    this.username = username;
  }
}
