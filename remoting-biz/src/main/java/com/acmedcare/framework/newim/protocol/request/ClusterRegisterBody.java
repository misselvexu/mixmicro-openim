package com.acmedcare.framework.newim.protocol.request;

import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Cluster Register Body
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class ClusterRegisterBody implements Serializable {

  private static final long serialVersionUID = 107938806075162869L;
  private List<WssInstance> wssInstances;

  @Getter
  @Setter
  public static class WssInstance implements Serializable {

    private String wssName;
    private String wssHost;
    private int wssPort;
  }
}
