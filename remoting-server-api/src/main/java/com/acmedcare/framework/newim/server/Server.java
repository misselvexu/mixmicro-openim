package com.acmedcare.framework.newim.server;

import com.acmedcare.framework.newim.Namespace;
import com.acmedcare.framework.newim.spi.Extensible;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 * Server Interface Api Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@Extensible
public interface Server {

  /**
   * Server Startup Method
   *
   * @param properties server config properties
   * @return server instance
   */
  Server startup(ServerProperties properties);

  /**
   * Shutdown Server
   *
   * <p>
   */
  void shutdown();

  /**
   * Server Properties
   *
   * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
   * @version ${project.version} - 2018-12-12.
   */
  @Getter
  @Setter
  public static class ServerProperties implements Serializable {

    private static final long serialVersionUID = -6552983472209783932L;

    /** Server Host Defined */
    private String host;

    /** Server Port Defined */
    private int port;

    /** Server Namespace Defined */
    private Namespace namespace;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ServerProperties)) {
        return false;
      }
      ServerProperties that = (ServerProperties) o;
      return getPort() == that.getPort()
          && getHost().equals(that.getHost())
          && getNamespace() == that.getNamespace();
    }

    @Override
    public int hashCode() {
      return Objects.hash(getHost(), getPort(), getNamespace());
    }
  }
}
