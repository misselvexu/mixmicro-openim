package com.acmedcare.framework.newim.server.replica;

import com.acmedcare.framework.newim.InstanceType;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * NodeReplicaProperties
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
@Data
@ConfigurationProperties(prefix = NodeReplicaProperties.NODE_REPLICA_PREFIX)
public class NodeReplicaProperties {

  public static final String NODE_REPLICA_PREFIX = "remoting.server";

  /** Base packages defined */
  @Deprecated private String basePackages = "com.acmedcare.framework";

  private Map<InstanceType, ReplicaProperties> replicas = Maps.newHashMap();

  @Data
  public static class ReplicaProperties {

    private boolean enabled = false;

    /** Replica host */
    private String host;

    /** Replica port */
    private int port;

    /** Replica instances Refresh period , default is : 5000ms */
    private long instancesRefreshPeriod = 5000;

    /** Replica Server & Connector Startup delay (ms) */
    private long startupDelay = 5000;

    /** Replica Request timeout (ms) ,default: 3000 */
    private long requestTimeout = 3000;

    /** NodeReplicaService Defined */
    private String replicaServiceClass;

    /** Replica Connector Client Heartbeat Enabled Flag */
    private boolean connectorHeartbeatEnabled = true;

    /** Replica Connector Client Heartbeat Period (ms) */
    private int connectorHeartbeatPeriod = 20000;

    /** Replica Connector connection state failed , max retry times , 0 is always retry */
    private int connectorMaxRetryTimes = 0;

    /** Retry Period Time (ms) */
    private int connectorRetryPeriod = 1000;
  }
}
