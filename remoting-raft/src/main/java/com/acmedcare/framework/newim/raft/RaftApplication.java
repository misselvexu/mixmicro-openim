package com.acmedcare.framework.newim.raft;

import io.atomix.cluster.ClusterConfig;
import io.atomix.cluster.Node;
import io.atomix.cluster.discovery.BootstrapDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.protocols.backup.partition.PrimaryBackupPartitionGroup;
import io.atomix.protocols.raft.partition.RaftPartitionGroup;
import java.io.File;

/**
 * RaftApplication
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
public class RaftApplication {

  public static void main(String[] args) {

    ClusterConfig clusterConfig = new ClusterConfig();
    Atomix atomix =
        Atomix.builder()
            .withClusterId("raft-cluster-1")
            .withAddress("127.0.0.1:3444")
            .withMembershipProvider(
                BootstrapDiscoveryProvider.builder()
                    .withNodes(
                        Node.builder()
                            .withId("raft-server-1")
                            .withAddress("127.0.0.1:3444")
                            .build(),
                        Node.builder()
                            .withId("raft-server-2")
                            .withAddress("127.0.0.1:3445")
                            .build(),
                        Node.builder()
                            .withId("raft-server-3")
                            .withAddress("127.0.0.1:3446")
                            .build())
                    .build())
            .withManagementGroup(
                RaftPartitionGroup.builder("master")
                    .withPartitionSize(1)
                    .withMembers("raft-server-1" /*, "raft-server-2", "raft-server-3"*/)
                    .withDataDirectory(
                        new File(String.format("target/test-logs/%s", "raft-cluster-1")))
                    .build())
            .withPartitionGroups(
                PrimaryBackupPartitionGroup.builder("data").withNumPartitions(32).build())
            .build();

    atomix.start().join();
  }
}
