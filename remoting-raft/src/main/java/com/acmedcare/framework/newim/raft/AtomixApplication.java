package com.acmedcare.framework.newim.raft;

import io.atomix.cluster.AtomixCluster;
import io.atomix.cluster.ClusterMembershipEvent;
import io.atomix.cluster.ClusterMembershipEventListener;
import io.atomix.cluster.Member;
import io.atomix.cluster.discovery.MulticastDiscoveryProvider;
import io.atomix.core.Atomix;
import io.atomix.utils.net.Address;
import java.time.Duration;
import java.util.Collection;

/**
 * AtomixApplication
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
public class AtomixApplication {

  public static void main(String[] args) {

    AtomixCluster cluster =
        AtomixCluster.builder()
            .withClusterId("cluster-1")
            .withMemberId("member-1")
            .withAddress("127.0.0.1:5679")
            .withMulticastEnabled()
            .withMulticastAddress(Address.from("230.0.0.1:54321"))
            .withMembershipProvider(
                MulticastDiscoveryProvider.builder()
                    .withBroadcastInterval(Duration.ofSeconds(1))
                    .build())
            .build();
    cluster.start().join();

    print(cluster);

    cluster
        .getMembershipService()
        .addListener(
            new ClusterMembershipEventListener() {
              @Override
              public void event(ClusterMembershipEvent event) {
                System.out.println(">>>>" + event.subject() + "->" + event.type());
                print(cluster);
              }
            });
  }

  private static void print(AtomixCluster cluster) {
    System.out.println("---------------------");
    Collection<Member> members = cluster.getMembershipService().getMembers();
    for (Member member : members) {
      System.out.println(member.id() + " ,Active: " + member.isActive());
    }
    System.out.println();
  }

  public static class Member1 {
    public static void main(String[] args) {
      Atomix atomix =
          Atomix.builder()
              .withMemberId("member1")
              .withAddress("127.0.0.1:5680")
              .withProperty("m1key", "m1value")
              .withMulticastEnabled()
              .withMulticastAddress(Address.from("230.0.0.1:54321"))
              .build();
      atomix.start();

      atomix
          .getMembershipService()
          .addListener(
              new ClusterMembershipEventListener() {
                @Override
                public void event(ClusterMembershipEvent event) {
                  System.out.println(">>>>" + event.subject() + "->" + event.type());
                }
              });
    }
  }

  public static class Member2 {
    public static void main(String[] args) {
      Atomix atomix =
          Atomix.builder()
              .withMemberId("member2")
              .withAddress("127.0.0.1:5681")
              .withMulticastEnabled()
              .build();

      atomix.start();

      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  for (; ; ) {
                    try {
                      Thread.sleep(3000);

                      Collection<Member> members =
                          atomix.getMembershipService().getReachableMembers();

                      System.out.println("当前可用节点: " + members);

                    } catch (Exception ignored) {

                    }
                  }
                }
              })
          .start();
      System.out.println("startup");
    }
  }
}
