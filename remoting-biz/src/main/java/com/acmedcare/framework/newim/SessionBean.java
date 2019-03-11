package com.acmedcare.framework.newim;

import com.acmedcare.framework.newim.client.MessageConstants;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * Session Bean Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-11.
 */
@Getter
@Setter
@NoArgsConstructor
public class SessionBean implements Serializable {

  private static final long serialVersionUID = 8508516905909911318L;

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

  /**
   * Defined Session Id ,
   *
   * <p>Like:
   * <li>PassportId
   * <li>DeviceId
   */
  private String sessionId;

  @Builder
  public SessionBean(String namespace, String sessionId) {
    this.namespace = namespace;
    this.sessionId = sessionId;
    if (this.namespace == null) {
      this.namespace = MessageConstants.DEFAULT_NAMESPACE;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SessionBean that = (SessionBean) o;
    return Objects.equals(namespace, that.namespace) && Objects.equals(sessionId, that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, sessionId);
  }

  @Override
  public String toString() {
    return "SessionBean{"
        + "namespace='"
        + namespace
        + '\''
        + ", sessionId='"
        + sessionId
        + '\''
        + '}';
  }
}
