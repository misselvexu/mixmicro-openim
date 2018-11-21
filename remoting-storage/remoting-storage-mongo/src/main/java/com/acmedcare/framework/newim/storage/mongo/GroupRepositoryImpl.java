package com.acmedcare.framework.newim.storage.mongo;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.storage.mongo.IMStorageCollections.REF_GROUP_MEMBER;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.mongodb.MongoClient;
import com.mongodb.client.ClientSession;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * Group Repository Impl
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Repository
public class GroupRepositoryImpl implements GroupRepository {
  private final MongoTemplate mongoTemplate;
  private final MongoTransactionManager mongoTransactionManager;
  private final MongoClient mongoClient;

  @Autowired
  public GroupRepositoryImpl(
      MongoTemplate mongoTemplate,
      MongoTransactionManager mongoTransactionManager,
      MongoClient mongoClient) {
    this.mongoTemplate = mongoTemplate;
    this.mongoTransactionManager = mongoTransactionManager;
    this.mongoClient = mongoClient;
  }

  @Override
  public void saveGroup(Group group) {
    boolean exist =
        mongoTemplate.exists(
            new Query(Criteria.where("groupId").is(group.getGroupId())),
            IMStorageCollections.GROUP.collectionName());

    if (!exist) {
      mongoTemplate.save(group, IMStorageCollections.GROUP.collectionName());
    } else {
      mongoLog.warn("[NEW-IM-DB] 群组:{},已经存在,不重复添加", group.getGroupId());
    }
  }

  @Override
  public void saveGroupMembers(GroupMembers members) {
    // REF_GROUP_MEMBER
    if (members.getMemberIds() != null && members.getMemberIds().size() > 0) {
      Query query =
          new Query(
              Criteria.where("groupId")
                  .is(members.getGroupId())
                  .and("memberId")
                  .in(members.getMemberIds()));

      // TODO transaction
      ClientSession clientSession = mongoClient.startSession();
      clientSession.startTransaction();
      try {
        mongoTemplate.findAllAndRemove(query, REF_GROUP_MEMBER.collectionName());
        List<GroupMemberRef> refs = new ArrayList<>();
        members
            .getMemberIds()
            .forEach(
                memberId ->
                    refs.add(
                        GroupMemberRef.builder()
                            .groupId(members.getGroupId())
                            .memberId(memberId)
                            .build()));

        mongoTemplate.insert(refs, REF_GROUP_MEMBER.collectionName());
        //
        clientSession.commitTransaction();
      } catch (Exception e) {
        clientSession.abortTransaction();
      }
    }
  }

  @Getter
  @Setter
  private static class GroupMemberRef {
    private String groupId;
    private String memberId;

    @Builder
    public GroupMemberRef(String groupId, String memberId) {
      this.groupId = groupId;
      this.memberId = memberId;
    }
  }
}
