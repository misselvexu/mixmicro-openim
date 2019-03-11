package com.acmedcare.framework.newim.storage.mongo;

import com.acmedcare.framework.newim.Group;
import com.acmedcare.framework.newim.Group.GroupMembers;
import com.acmedcare.framework.newim.GroupMemberRef;
import com.acmedcare.framework.newim.Status;
import com.acmedcare.framework.newim.client.bean.Member;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.exception.StorageException;
import com.google.common.collect.Lists;
import com.mongodb.MongoClient;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.acmedcare.framework.newim.CommonLogger.mongoLog;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.GROUP;
import static com.acmedcare.framework.newim.storage.IMStorageCollections.REF_GROUP_MEMBER;
import static org.springframework.data.mongodb.SessionSynchronization.ALWAYS;
import static org.springframework.data.mongodb.SessionSynchronization.ON_ACTUAL_TRANSACTION;

/**
 * Group Repository Impl
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
public class GroupRepositoryImpl implements GroupRepository {
  private final MongoTemplate mongoTemplate;
  private final TransactionTemplate transactionTemplate;
  private final MongoClient mongoClient;

  public GroupRepositoryImpl(
      MongoTemplate mongoTemplate,
      MongoTransactionManager mongoTransactionManager,
      MongoClient mongoClient,
      TransactionTemplate transactionTemplate) {
    this.mongoTemplate = mongoTemplate;
    this.mongoClient = mongoClient;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public Group queryGroup(String namespace, String groupId) {
    return mongoTemplate.findOne(
        new Query(Criteria.where("groupId").is(groupId).and("namespace").is(namespace)),
        Group.class,
        GROUP);
  }

  @Override
  public void saveGroup(Group group) {
    boolean exist =
        mongoTemplate.exists(
            new Query(
                Criteria.where("groupId")
                    .is(group.getGroupId())
                    .and("namespace")
                    .is(group.getNamespace())),
            GROUP);

    if (!exist) {
      mongoTemplate.save(group, GROUP);
    } else {
      mongoLog.warn("群组:{},已经存在,不重复添加", group.getGroupId());
    }
  }

  @Override
  public void updateGroup(Group group) {
    Query query =
        new Query(
            Criteria.where("groupId")
                .is(group.getGroupId())
                .and("namespace")
                .is(group.getNamespace()));
    Update update = new Update();
    update.set("groupName", group.getGroupName());
    update.set("groupOwner", group.getGroupOwner());
    update.set("groupBizTag", group.getGroupBizTag());
    update.set("groupExt", group.getGroupExt());
    UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, GROUP);
    long row = updateResult.getModifiedCount();
    mongoLog.info("请求更新群组返回值:{}", row);
  }

  @Override
  public long removeGroup(String namespace, String groupId) {
    mongoLog.info("请求删除群组:{}", groupId);
    Query query = new Query(Criteria.where("groupId").is(groupId).and("namespace").is(namespace));
    Update update = new Update();
    update.set("groupStatus", Status.DISABLED);
    UpdateResult updateResult = this.mongoTemplate.updateFirst(query, update, GROUP);

    // change remove rule ,flag
    /*
    DeleteResult dr1 = mongoTemplate.remove(query, GROUP);
    mongoLog.info("删除群组影响行数:{}", dr1.getDeletedCount());
    DeleteResult dr2 = mongoTemplate.remove(query, REF_GROUP_MEMBER);
    mongoLog.info("删除群组与成员关联关系记录影响行数:{}", dr2.getDeletedCount());
    */

    return updateResult.getModifiedCount();
  }

  @Override
  public void saveGroupMembers(GroupMembers members) {
    // REF_GROUP_MEMBER
    if (members.getMembers() != null && members.getMembers().size() > 0) {

      if (!mongoTemplate.exists(
          new Query(
              Criteria.where("groupId")
                  .is(members.getGroupId())
                  .and("namespace")
                  .is(members.getNamespace())),
          GROUP)) {
        throw new StorageException("群组:" + members.getGroupId() + "不存在");
      }

      List<Member> memberList = members.getMembers();
      List<String> memberIds = Lists.newArrayList();
      for (Member member : memberList) {
        memberIds.add(member.getMemberId().toString());
      }

      Query query =
          new Query(
              Criteria.where("groupId")
                  .is(members.getGroupId())
                  .and("namespace")
                  .is(members.getNamespace())
                  .and("memberId")
                  .in(memberIds));

      mongoTemplate.setSessionSynchronization(ALWAYS);
      AtomicBoolean reset = new AtomicBoolean(false);
      Boolean result =
          transactionTemplate.execute(
              new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                  try {

                    DeleteResult deleteResult = mongoTemplate.remove(query, REF_GROUP_MEMBER);
                    mongoLog.info("预删除行数:{} ", deleteResult.getDeletedCount());
                    List<GroupMemberRef> refs = new ArrayList<>();
                    members
                        .getMembers()
                        .forEach(
                            member ->
                                refs.add(
                                    GroupMemberRef.builder()
                                        .namespace(members.getNamespace())
                                        .groupId(members.getGroupId())
                                        .memberId(member.getMemberId().toString())
                                        .memberName(member.getMemberName())
                                        .memberUserName(member.getMemberUserName())
                                        .memberExt(member.getMemberExt())
                                        .portrait(member.getPortrait())
                                        .build()));

                    mongoTemplate.insert(refs, REF_GROUP_MEMBER);

                    return true;
                  } catch (Exception e) {
                    mongoLog.error("添加群组成员方法异常回滚", e);
                    transactionStatus.setRollbackOnly();

                    mongoLog.info("[FIX-ED MONGO-4.0.4] 尝试非事务执行操作[不安全]");
                    // TODO FIX: PROCESS FAILED WITH
                    //  REASON `It is illegal to run command createIndexes in a multi-document
                    //  transaction`
                    //  Maybe is mongo-4.0.4 bug
                    //  retry:
                    if (reset.compareAndSet(false, true)) {

                      try {
                        mongoTemplate.setSessionSynchronization(ON_ACTUAL_TRANSACTION);

                        // WARN : here is no transaction
                        DeleteResult deleteResult = mongoTemplate.remove(query, REF_GROUP_MEMBER);
                        mongoLog.info("预删除行数:{} ", deleteResult.getDeletedCount());
                        List<GroupMemberRef> refs = new ArrayList<>();
                        members
                            .getMembers()
                            .forEach(
                                member ->
                                    refs.add(
                                        GroupMemberRef.builder()
                                            .namespace(members.getNamespace())
                                            .groupId(members.getGroupId())
                                            .memberId(member.getMemberId().toString())
                                            .memberName(member.getMemberName())
                                            .memberExt(member.getMemberExt())
                                            .memberUserName(member.getMemberUserName())
                                            .portrait(member.getPortrait())
                                            .build()));

                        mongoTemplate.insert(refs, REF_GROUP_MEMBER);
                        return true;

                      } catch (Exception ex) {
                        return false;
                      }
                    } else {
                      return false;
                    }

                  } finally {
                    if (reset.compareAndSet(false, true)) {
                      mongoTemplate.setSessionSynchronization(ON_ACTUAL_TRANSACTION);
                    }
                  }
                }
              });

      if (result == null || !result) {
        throw new StorageException("群组添加人员失败");
      }
    }
  }

  @Override
  public long removeGroupMembers(String namespace, String groupId, List<String> memberIds) {

    mongoLog.info("请求删除群组:{},成员列表:{}", groupId, Arrays.toString(memberIds.toArray()));
    Query query =
        new Query(
            Criteria.where("groupId")
                .is(groupId)
                .and("namespace")
                .is(namespace)
                .and("memberId")
                .in(memberIds));
    DeleteResult deleteResult = mongoTemplate.remove(query, REF_GROUP_MEMBER);
    mongoLog.info("删除群组成员影响行数:{}", deleteResult.getDeletedCount());
    return deleteResult.getDeletedCount();
  }

  @Override
  public List<String> queryGroupMemberIds(String namespace, String groupId) {
    Query query = new Query(Criteria.where("groupId").is(groupId).and("namespace").is(namespace));
    return mongoTemplate.findDistinct(query, "memberId", REF_GROUP_MEMBER, String.class);
  }

  @Override
  public List<GroupMemberRef> queryGroupMembers(String namespace, String groupId) {
    Query query = new Query(Criteria.where("groupId").is(groupId).and("namespace").is(namespace));
    return mongoTemplate.find(query, GroupMemberRef.class, REF_GROUP_MEMBER);
  }

  @Override
  public List<Member> queryGroupMembersList(String namespace, String groupId) {
    List<GroupMemberRef> refs = queryGroupMembers(namespace, groupId);
    List<Member> members = Lists.newArrayList();
    for (GroupMemberRef ref : refs) {
      members.add(
          Member.builder()
              .memberId(Long.parseLong(ref.getMemberId()))
              .memberName(ref.getMemberName())
              .memberUserName(ref.getMemberUserName())
              .memberExt(ref.getMemberExt())
              .portrait(ref.getPortrait())
              .build());
    }
    return members;
  }

  @Override
  public List<Group> queryMemberGroups(String namespace, String passportId) {

    Query groupIdsQuery =
        new Query(Criteria.where("memberId").is(passportId).and("namespace").is(namespace));
    List<String> groupIds =
        this.mongoTemplate.findDistinct(groupIdsQuery, "groupId", REF_GROUP_MEMBER, String.class);
    if (!groupIds.isEmpty()) {
      Query groupDetailQuery = new Query(Criteria.where("groupId").in(groupIds));
      return this.mongoTemplate.find(groupDetailQuery, Group.class, GROUP);
    }
    return Lists.newArrayList();
  }

  @Override
  public List<Group> queryGroupList(String namespace, String groupBizType) {
    Query query = new Query(Criteria.where("groupBizTag").is(groupBizType).and("namespace").is(namespace));
    return this.mongoTemplate.find(query, Group.class, GROUP);
  }
}
