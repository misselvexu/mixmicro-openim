package com.acmedcare.framework.newim.raft;

import com.acmedcare.framework.newim.raft.protocol.RaftServerMessagingProtocol;
import io.atomix.cluster.MemberId;
import io.atomix.cluster.messaging.MessagingConfig;
import io.atomix.cluster.messaging.MessagingService;
import io.atomix.cluster.messaging.impl.NettyMessagingService;
import io.atomix.primitive.operation.OperationType;
import io.atomix.primitive.operation.PrimitiveOperation;
import io.atomix.primitive.operation.impl.DefaultOperationId;
import io.atomix.primitive.session.SessionId;
import io.atomix.protocols.raft.RaftError;
import io.atomix.protocols.raft.RaftServer;
import io.atomix.protocols.raft.ReadConsistency;
import io.atomix.protocols.raft.cluster.RaftMember;
import io.atomix.protocols.raft.cluster.RaftMember.Type;
import io.atomix.protocols.raft.cluster.impl.DefaultRaftMember;
import io.atomix.protocols.raft.protocol.AppendRequest;
import io.atomix.protocols.raft.protocol.AppendResponse;
import io.atomix.protocols.raft.protocol.CloseSessionRequest;
import io.atomix.protocols.raft.protocol.CloseSessionResponse;
import io.atomix.protocols.raft.protocol.CommandRequest;
import io.atomix.protocols.raft.protocol.CommandResponse;
import io.atomix.protocols.raft.protocol.ConfigureRequest;
import io.atomix.protocols.raft.protocol.ConfigureResponse;
import io.atomix.protocols.raft.protocol.InstallRequest;
import io.atomix.protocols.raft.protocol.InstallResponse;
import io.atomix.protocols.raft.protocol.JoinRequest;
import io.atomix.protocols.raft.protocol.JoinResponse;
import io.atomix.protocols.raft.protocol.KeepAliveRequest;
import io.atomix.protocols.raft.protocol.KeepAliveResponse;
import io.atomix.protocols.raft.protocol.LeaveRequest;
import io.atomix.protocols.raft.protocol.LeaveResponse;
import io.atomix.protocols.raft.protocol.MetadataRequest;
import io.atomix.protocols.raft.protocol.MetadataResponse;
import io.atomix.protocols.raft.protocol.OpenSessionRequest;
import io.atomix.protocols.raft.protocol.OpenSessionResponse;
import io.atomix.protocols.raft.protocol.PollRequest;
import io.atomix.protocols.raft.protocol.PollResponse;
import io.atomix.protocols.raft.protocol.PublishRequest;
import io.atomix.protocols.raft.protocol.QueryRequest;
import io.atomix.protocols.raft.protocol.QueryResponse;
import io.atomix.protocols.raft.protocol.RaftResponse;
import io.atomix.protocols.raft.protocol.ReconfigureRequest;
import io.atomix.protocols.raft.protocol.ReconfigureResponse;
import io.atomix.protocols.raft.protocol.ResetRequest;
import io.atomix.protocols.raft.protocol.VoteRequest;
import io.atomix.protocols.raft.protocol.VoteResponse;
import io.atomix.protocols.raft.storage.RaftStorage;
import io.atomix.protocols.raft.storage.log.entry.CloseSessionEntry;
import io.atomix.protocols.raft.storage.log.entry.CommandEntry;
import io.atomix.protocols.raft.storage.log.entry.ConfigurationEntry;
import io.atomix.protocols.raft.storage.log.entry.InitializeEntry;
import io.atomix.protocols.raft.storage.log.entry.KeepAliveEntry;
import io.atomix.protocols.raft.storage.log.entry.MetadataEntry;
import io.atomix.protocols.raft.storage.log.entry.OpenSessionEntry;
import io.atomix.protocols.raft.storage.log.entry.QueryEntry;
import io.atomix.protocols.raft.storage.system.Configuration;
import io.atomix.storage.StorageLevel;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Namespace;
import io.atomix.utils.serializer.Serializer;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RaftServerTest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class RaftServerTest {
  private static final Serializer protocolSerializer =
      Serializer.using(
          Namespace.builder()
              .register(OpenSessionRequest.class)
              .register(OpenSessionResponse.class)
              .register(CloseSessionRequest.class)
              .register(CloseSessionResponse.class)
              .register(KeepAliveRequest.class)
              .register(KeepAliveResponse.class)
              .register(QueryRequest.class)
              .register(QueryResponse.class)
              .register(CommandRequest.class)
              .register(CommandResponse.class)
              .register(MetadataRequest.class)
              .register(MetadataResponse.class)
              .register(JoinRequest.class)
              .register(JoinResponse.class)
              .register(LeaveRequest.class)
              .register(LeaveResponse.class)
              .register(ConfigureRequest.class)
              .register(ConfigureResponse.class)
              .register(ReconfigureRequest.class)
              .register(ReconfigureResponse.class)
              .register(InstallRequest.class)
              .register(InstallResponse.class)
              .register(PollRequest.class)
              .register(PollResponse.class)
              .register(VoteRequest.class)
              .register(VoteResponse.class)
              .register(AppendRequest.class)
              .register(AppendResponse.class)
              .register(PublishRequest.class)
              .register(ResetRequest.class)
              .register(RaftResponse.Status.class)
              .register(RaftError.class)
              .register(RaftError.Type.class)
              .register(PrimitiveOperation.class)
              .register(ReadConsistency.class)
              .register(byte[].class)
              .register(long[].class)
              .register(CloseSessionEntry.class)
              .register(CommandEntry.class)
              .register(ConfigurationEntry.class)
              .register(InitializeEntry.class)
              .register(KeepAliveEntry.class)
              .register(MetadataEntry.class)
              .register(OpenSessionEntry.class)
              .register(QueryEntry.class)
              .register(PrimitiveOperation.class)
              .register(DefaultOperationId.class)
              .register(OperationType.class)
              .register(ReadConsistency.class)
              .register(ArrayList.class)
              .register(Collections.emptyList().getClass())
              .register(HashSet.class)
              .register(DefaultRaftMember.class)
              .register(MemberId.class)
              .register(SessionId.class)
              .register(RaftMember.Type.class)
              .register(Instant.class)
              .register(Configuration.class)
              .build());

  private static final Namespace storageNamespace =
      Namespace.builder()
          .register(CloseSessionEntry.class)
          .register(CommandEntry.class)
          .register(ConfigurationEntry.class)
          .register(InitializeEntry.class)
          .register(KeepAliveEntry.class)
          .register(MetadataEntry.class)
          .register(OpenSessionEntry.class)
          .register(QueryEntry.class)
          .register(PrimitiveOperation.class)
          .register(DefaultOperationId.class)
          .register(OperationType.class)
          .register(ReadConsistency.class)
          .register(ArrayList.class)
          .register(HashSet.class)
          .register(DefaultRaftMember.class)
          .register(MemberId.class)
          .register(RaftMember.Type.class)
          .register(Instant.class)
          .register(Configuration.class)
          .register(byte[].class)
          .register(long[].class)
          .build();

  private static Map<MemberId, Address> addressMap = new ConcurrentHashMap<>();

  public static void main(String[] args) {

    RaftMember member =
        new DefaultRaftMember(MemberId.from("raft-server-1"), Type.ACTIVE, Instant.now());

    Address address = Address.from(21111);
    MessagingService messagingManager =
        new NettyMessagingService("test", address, new MessagingConfig()).start().join();

    RaftServer.Builder builder =
        RaftServer.builder(member.memberId())
            .withMembershipService(null)
            .withProtocol(
                new RaftServerMessagingProtocol(
                    messagingManager, protocolSerializer, addressMap::get))
            .withStorage(
                RaftStorage.builder()
                    .withStorageLevel(StorageLevel.DISK)
                    .withDirectory(
                        new File(String.format("target/fuzz-logs/%s", member.memberId())))
                    .withNamespace(storageNamespace)
                    .withMaxSegmentSize(1024 * 1024)
                    .build());

    RaftServer server = builder.build();
    server.bootstrap(member.memberId());
  }
}
