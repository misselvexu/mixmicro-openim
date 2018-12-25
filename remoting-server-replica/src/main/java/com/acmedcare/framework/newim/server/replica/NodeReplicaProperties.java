package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.InstanceType;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NodeReplicaProperties
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "remoting.server.replicas")
public class NodeReplicaProperties {

  private Map<InstanceType, ReplicaProperties> nodeConfigList;

  @Getter
  @Setter
  public static class ReplicaProperties {
    private boolean enabled = false;
    /** Replica instances Refresh period , default is : 5000ms */
    private long instancesRefreshPeriod = 5000;

    /** NodeReplicaService Defined */
    private String replicaServiceClass;
  }
}
