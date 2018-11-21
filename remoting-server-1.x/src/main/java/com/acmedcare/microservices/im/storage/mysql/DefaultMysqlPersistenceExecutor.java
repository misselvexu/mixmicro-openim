package com.acmedcare.microservices.im.storage.mysql;

import com.acmedcare.microservices.im.biz.bean.Account;
import com.acmedcare.microservices.im.biz.bean.Group;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.bean.Message.GroupMessage;
import com.acmedcare.microservices.im.biz.bean.Message.InnerType;
import com.acmedcare.microservices.im.biz.bean.Message.MessageType;
import com.acmedcare.microservices.im.biz.bean.Message.SingleMessage;
import com.acmedcare.microservices.im.biz.bean.Session;
import com.acmedcare.microservices.im.biz.request.PushMessageStatusHeader.PMT;
import com.acmedcare.microservices.im.core.ServerFacade.MessageNotify;
import com.acmedcare.microservices.im.exception.DataAccessException;
import com.acmedcare.microservices.im.kits.Charsets;
import com.acmedcare.microservices.im.storage.IPersistenceExecutor;
import com.google.common.collect.Lists;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Mysql Implements
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Component
public class DefaultMysqlPersistenceExecutor implements IPersistenceExecutor {

  private final JdbcTemplate jdbcTemplate;

  private final TransactionTemplate transactionTemplate;

  @Autowired
  public DefaultMysqlPersistenceExecutor(
      JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public void saveMessage(
      final List<Object[]> messages,
      final List<Object[]> messageSenders,
      final List<Object[]> messageReceivers) {

    transactionTemplate.execute(
        (TransactionCallback<Void>)
            transactionStatus -> {
              try {

                // 1. im_message_record
                String sql =
                    "insert into im_message_record(message_id,message_content,sender,message_type,receiver_type,send_timestamp,receiver,receiver_group) values (?,?,?,?,?,NOW(),?,?)";

                jdbcTemplate.batchUpdate(sql, messages);

                // 2. im_message_send
                sql =
                    "insert into im_message_send(message_id,sender,receiver,group_id) values (?,?,?,?) ";
                jdbcTemplate.batchUpdate(sql, messageSenders);

                // 3. im_message_receive
                sql =
                    "insert into im_message_receive(message_id,sender,receiver,group_id) values (?,?,?,?) ";
                jdbcTemplate.batchUpdate(sql, messageReceivers);

              } catch (Exception e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
              }
              return null;
            });
  }

  /**
   * Query Account Groups
   *
   * @param username passport
   * @return list
   */
  @Override
  public List<Group> queryAccountGroups(String username) {

    String sql = "select group_code ,group_name from im_refs_group_member where member_name = ? ";
    return this.jdbcTemplate.query(
        sql,
        new Object[] {username},
        (rs, rowNum) ->
            Group.builder()
                .name(rs.getString("group_name"))
                .code(rs.getString("group_code"))
                .build());
  }

  /**
   * Query Account Group Message List
   *
   * @param username passport
   * @param sender group flag
   * @param leastMessageId least message sender
   * @param limit limit size
   * @return list
   */
  @Override
  public List<Message> queryAccountGroupMessages(
      String username, String sender, int type, long leastMessageId, long limit)
      throws DataAccessException {

    try {

      limit = (limit <= 0 ? 20 : limit);

      if (type == 0) {
        // 单聊消息
        if (leastMessageId <= 0) {
          // 无最新消息 ID, 获取最新的消息
          String sql =
              "select t2.* from im_message_receive t1 left join im_message_record t2 on t1.message_id = t2.message_id where t1.receiver = ? and t2.sender = ? order by t2.send_timestamp desc limit ?";

          return this.jdbcTemplate.query(
              sql,
              new Object[] {username, sender, limit},
              new RowMapper<Message>() {
                @Override
                public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return SingleMessage.builder()
                      .receiver(rs.getString("receiver"))
                      .body(Charsets.bytes(rs.getString("message_content")))
                      .mid(rs.getLong("message_id"))
                      .messageType(MessageType.SINGLE)
                      .sendTimestamp(rs.getTimestamp("send_timestamp"))
                      .innerType(
                          rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                      .sender(rs.getString("sender"))
                      .build();
                }
              });

        } else {
          // 查历史
          String sql =
              "select t2.* from im_message_receive t1 left join im_message_record t2 on t1.message_id = t2.message_id where t1.receiver = ? and t2.sender = ? and t2.message_id < ? order by t2.send_timestamp desc limit ?";

          return this.jdbcTemplate.query(
              sql,
              new Object[] {username, sender, leastMessageId, limit},
              new RowMapper<Message>() {
                @Override
                public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return SingleMessage.builder()
                      .receiver(rs.getString("receiver"))
                      .body(Charsets.bytes(rs.getString("message_content")))
                      .mid(rs.getLong("message_id"))
                      .messageType(MessageType.SINGLE)
                      .sendTimestamp(rs.getTimestamp("send_timestamp"))
                      .innerType(
                          rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                      .sender(rs.getString("sender"))
                      .build();
                }
              });
        }
      }

      if (type == 1) {
        // 群组消息
        if (leastMessageId <= 0) {
          // 无最新消息 ID, 获取最新的消息
          String sql =
              "select t2.* from im_message_receive t1 left join im_message_record t2 on t1.message_id = t2.message_id where t1.receiver = ? and t2.receiver_group = ? order by t2.send_timestamp desc limit ?";

          return this.jdbcTemplate.query(
              sql,
              new Object[] {username, sender, limit},
              new RowMapper<Message>() {
                @Override
                public GroupMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return GroupMessage.builder()
                      .group(rs.getString("receiver_group"))
                      .body(Charsets.bytes(rs.getString("message_content")))
                      .mid(rs.getLong("message_id"))
                      .messageType(MessageType.GROUP)
                      .sendTimestamp(rs.getTimestamp("send_timestamp"))
                      .innerType(
                          rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                      .sender(rs.getString("sender"))
                      .build();
                }
              });

        } else {
          // 查历史
          String sql =
              "select t2.* from im_message_receive t1 left join im_message_record t2 on t1.message_id = t2.message_id where t1.receiver = ? and t2.receiver_group = ? and t2.message_id < ? order by t2.send_timestamp desc limit ?";

          return this.jdbcTemplate.query(
              sql,
              new Object[] {username, sender, leastMessageId, limit},
              new RowMapper<Message>() {
                @Override
                public GroupMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return GroupMessage.builder()
                      .group(rs.getString("receiver_group"))
                      .body(Charsets.bytes(rs.getString("message_content")))
                      .mid(rs.getLong("message_id"))
                      .messageType(MessageType.GROUP)
                      .sendTimestamp(rs.getTimestamp("send_timestamp"))
                      .innerType(
                          rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                      .sender(rs.getString("sender"))
                      .build();
                }
              });
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    throw new DataAccessException("无效的消息类型字段,expect type is [0,1]");
  }

  /**
   * save or update message read status
   *
   * @param username passport
   * @param pmt push message innerType
   * @param sender group code or passport
   * @param leastMessageId least message sender
   * @return result
   */
  @Override
  public boolean saveOrUpdateMessageReadStatus(
      String username, PMT pmt, String sender, long leastMessageId) {

    switch (pmt) {

        // 单聊消息已读上报
      case SINGLE:
        String sql =
            "update im_message_record set read_flag = 1 where sender = ?  and receiver = ? and message_id <= ?";

        int row = this.jdbcTemplate.update(sql, sender, username, leastMessageId);

        sql =
            "update im_session_records set has_new_message = 0 , least_message_id = ? where sender = ?  and receiver = ?";

        row = this.jdbcTemplate.update(sql, leastMessageId, sender, username);

        return true;

        // 群聊消息已读上报

      case GROUP:
        Boolean result =
            this.transactionTemplate.execute(
                new TransactionCallback<Boolean>() {
                  @Override
                  public Boolean doInTransaction(TransactionStatus transactionStatus) {

                    try {

                      String sql =
                          "delete from im_group_message_read_status where group_id = ?  and reader = ? ";
                      jdbcTemplate.update(sql, sender, username);

                      sql =
                          "insert into im_group_message_read_status (group_id,reader,read_timestamp,least_message_id) values (?,?,NOW(),?)";

                      jdbcTemplate.update(sql, sender, username, leastMessageId);

                      sql =
                          "update im_session_records set has_new_message = 0 , least_message_id = ? where group_name = ?  and receiver = ?";

                      jdbcTemplate.update(sql, leastMessageId, sender, username);

                    } catch (Exception e) {
                      transactionStatus.setRollbackOnly();
                      return false;
                    }
                    return true;
                  }
                });

        if (result != null) {
          return result;
        } else {
          return false;
        }
    }

    return false;
  }

  /** Create New Room */
  @Override
  public void newGroup(final String groupId, String groupName, String owner, Set<String> members) {

    this.transactionTemplate.execute(
        (TransactionCallback<Void>)
            transactionStatus -> {
              try {

                String sql =
                    "insert into `im_group_info` (group_name,group_code,owner) values (?,?,?)";

                String groupCode = groupId;
                if (StringUtils.isBlank(groupId)) {
                  groupCode = UUID.randomUUID().toString().replace("-", "");
                }
                // 1. save group
                int row = jdbcTemplate.update(sql, groupName, groupCode, owner);

                if (row <= 0) {
                  throw new DataAccessException("插入群组失败");
                }

                // 2. batch save members
                sql =
                    "insert into `im_refs_group_member` (group_code,group_name,member_name) values (?,?,?)";

                List<Object[]> params = Lists.newArrayList();

                for (String member : members) {
                  params.add(new Object[] {groupCode, groupName, member});
                }

                int[] rows = jdbcTemplate.batchUpdate(sql, params);

                if (Arrays.binarySearch(rows, 0) >= 0) {
                  throw new DataAccessException("批量插入用户群组关系失败");
                }

                //                transactionStatus.flush();

              } catch (Exception e) {
                e.printStackTrace();
                transactionStatus.setRollbackOnly();
              }
              return null;
            });
  }

  @Override
  public void addGroupMember(String groupId, Set<String> members) throws DataAccessException {

    this.transactionTemplate.execute(
        (TransactionCallback<Void>)
            transactionStatus -> {
              try {

                // 0. query group name
                String groupName =
                    jdbcTemplate.queryForObject(
                        "select group_name from im_group_info where group_code = ? ",
                        new Object[] {groupId},
                        String.class);

                if (StringUtils.isBlank(groupName)) {
                  throw new DataAccessException("无效的群组ID");
                }

                // 1. batch save members
                String sql =
                    "insert into `im_refs_group_member` (group_code,group_name,member_name) values (?,?,?)";

                List<Object[]> params = Lists.newArrayList();

                for (String member : members) {
                  params.add(new Object[] {groupId, groupName, member});
                }

                try {
                  int[] rows = jdbcTemplate.batchUpdate(sql, params);

                  if (Arrays.binarySearch(rows, 0) >= 0) {
                    throw new DataAccessException("批量插入用户群组关系失败");
                  }
                } catch (Throwable e) {
                  // ignore DuplicateKeyException
                  System.out.println("部分用户已经存在,忽略插入异常.");
                }

              } catch (Exception e) {
                transactionStatus.setRollbackOnly();
              }
              return null;
            });
  }

  @Override
  public void deleteGroupMember(String groupId, List<String> members) {
    this.transactionTemplate.execute(
        (TransactionCallback<Void>)
            transactionStatus -> {
              try {

                // 1. batch save members
                String sql =
                    "delete from `im_refs_group_member` where group_code = ? and member_name = ?";

                List<Object[]> params = Lists.newArrayList();

                for (String member : members) {
                  params.add(new Object[] {groupId, member});
                }

                int[] rows = jdbcTemplate.batchUpdate(sql, params);

                if (Arrays.binarySearch(rows, 0) >= 0) {
                  throw new DataAccessException("批量删除用户群组关系失败");
                }

              } catch (Exception e) {
                transactionStatus.setRollbackOnly();
              }
              return null;
            });
  }

  @Override
  public List<Session> queryAccountSessions(String username) {

    String sql =
        "select receiver,group_name,session_type,session_name,sender,has_new_message from im_session_records where receiver = ?";

    return this.jdbcTemplate.query(
        sql,
        new Object[] {username},
        (rs, rowNum) ->
            Session.builder()
                .type(rs.getInt("session_type"))
                .name(rs.getString("sender"))
                .groupName(rs.getString("group_name"))
                .unreadSize(rs.getInt("has_new_message"))
                .build());
  }

  @Override
  public List<Account> queryGroupMembers(String group) {

    String sql =
        "select distinct member_name from im_refs_group_member where group_code = ? or group_name = ? ";

    return this.jdbcTemplate.query(
        sql,
        new Object[] {group, group},
        (rs, rowNum) -> Account.builder().username(rs.getString("member_name")).build());
  }

  @Override
  public Session queryAccountSessionStatus(String username, int type, String flagId)
      throws DataAccessException {

    if (type == 0) {
      // 单聊
      StringBuffer sql = new StringBuffer();
      sql.append(" SELECT t2.* ");
      sql.append("   , ( ");
      sql.append("       SELECT COUNT(0) ");
      sql.append("       FROM `im_message_receive` ");
      sql.append("       WHERE (`sender` = ? ");
      sql.append("           AND `receiver` = ? ");
      sql.append("       AND `message_id` > ( ");
      sql.append("           SELECT `least_message_id` ");
      sql.append("       FROM `im_session_records` ");
      sql.append("       WHERE `receiver` = ? ");
      sql.append("       AND `sender` = ? ");
      sql.append("       )) ");
      sql.append("   ) AS `unread_size` ");
      sql.append(" FROM ( ");
      sql.append("     SELECT `least_message_id` ");
      sql.append(" FROM `im_session_records` ");
      sql.append(" WHERE `receiver` = ? ");
      sql.append(" AND `sender` = ? ");
      sql.append(" ) `temp1` ");
      sql.append(
          "       LEFT JOIN `im_message_record` `t2` ON `temp1`.`least_message_id` = `t2`.`message_id` LIMIT 1");

      return this.jdbcTemplate.queryForObject(
          sql.toString(),
          new Object[] {flagId, username, username, flagId, username, flagId},
          (rs, rowNum) ->
              Session.builder()
                  .type(0)
                  .name(rs.getString("sender"))
                  .unreadSize(rs.getInt("unread_size"))
                  .leastMessage(
                      SingleMessage.builder()
                          .innerType(
                              rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                          .messageType(MessageType.SINGLE)
                          .sender(rs.getString("sender"))
                          .sendTimestamp(rs.getTimestamp("send_timestamp"))
                          .mid(rs.getLong("message_id"))
                          .body(Charsets.bytes(rs.getString("message_content")))
                          .receiver(rs.getString("receiver"))
                          .build())
                  .build());
    }

    if (type == 1) {
      // 群组

      StringBuffer sql = new StringBuffer();
      sql.append(" SELECT t2.* ");
      sql.append("   , ( ");
      sql.append("       SELECT COUNT(0) ");
      sql.append("       FROM `im_message_receive` ");
      sql.append("       WHERE (`group_id` = ? ");
      sql.append("           AND `receiver` = ? ");
      sql.append("       AND `message_id` > ( ");
      sql.append("           SELECT `least_message_id` ");
      sql.append("       FROM `im_session_records` ");
      sql.append("       WHERE `receiver` = ? ");
      sql.append("       AND `group_id` = ? ");
      sql.append("       )) ");
      sql.append("   ) AS `unread_size` ");
      sql.append(" FROM ( ");
      sql.append("     SELECT `least_message_id` ");
      sql.append(" FROM `im_session_records` ");
      sql.append(" WHERE `receiver` = ? ");
      sql.append(" AND `group_name` = ? ");
      sql.append(" ) `temp1` ");
      sql.append(
          "       LEFT JOIN `im_message_record` `t2` ON `temp1`.`least_message_id` = `t2`.`message_id` LIMIT 1");

      return this.jdbcTemplate.queryForObject(
          sql.toString(),
          new Object[] {flagId, username, username, flagId, username, flagId},
          (rs, rowNum) ->
              Session.builder()
                  .type(1)
                  .name(rs.getString("receiver_group"))
                  .unreadSize(rs.getInt("unread_size"))
                  .leastMessage(
                      GroupMessage.builder()
                          .innerType(
                              rs.getInt("message_type") == 0 ? InnerType.NORMAL : InnerType.COMMAND)
                          .messageType(MessageType.GROUP)
                          .sender(rs.getString("sender"))
                          .sendTimestamp(rs.getTimestamp("send_timestamp"))
                          .mid(rs.getLong("message_id"))
                          .body(Charsets.bytes(rs.getString("message_content")))
                          .group(rs.getString("receiver_group"))
                          .build())
                  .build());
    }

    throw new DataAccessException("无效的消息类型字段,expect type is [0,1]");
  }

  @Override
  public void saveOrUpdateGroupSessionRecord(String group, Long mid, List<Account> groupReceivers) {

    /*
    String sql =
        "replace into im_session_records(session_name,create_time,session_type,sender,receiver,group_name,least_message_id) values (?,NOW(),?,NULL,?,?,?)";

    List<Object[]> params = Lists.newArrayList();
    for (Account groupReceiver : groupReceivers) {
      params.add(new Object[] {group, 1, groupReceiver.getUsername(), group, mid});
    }

    this.jdbcTemplate.batchUpdate(sql, params);
    */

    String sql = "delete from im_session_records where group_name =? and receiver = ? ";
    List<Object[]> params = Lists.newArrayList();
    for (Account groupReceiver : groupReceivers) {
      params.add(new Object[] {group, groupReceiver.getUsername()});
    }

    this.jdbcTemplate.batchUpdate(sql, params);

    sql =
        "insert into im_session_records(session_name,create_time,session_type,sender,receiver,group_name,least_message_id) values (?,NOW(),?,NULL,?,?,?)";
    params.clear();

    for (Account groupReceiver : groupReceivers) {
      params.add(new Object[] {group, 1, groupReceiver.getUsername(), group, mid});
    }

    this.jdbcTemplate.batchUpdate(sql, params);
  }

  @Override
  public void saveOrUpdateSingleSessionRecord(String sender, String receiver, Long messageId) {

    /*
    String r1 =
        "replace into im_session_records(session_name,create_time,session_type,sender,receiver,group_name,least_message_id) values (?,NOW(),?,?,?,NULL,?)";

    this.jdbcTemplate.update(r1, receiver, 0, sender, receiver, messageId);

    String r2 =
        "replace into im_session_records(session_name,create_time,session_type,sender,receiver,group_name,least_message_id) values (?,NOW(),?,?,?,NULL,?)";

    this.jdbcTemplate.update(r1, sender, 0, receiver, sender, messageId);
    */

    String sql =
        "update im_session_records set least_message_id = ? where sender = ? and receiver = ? and session_type = 0";

    int row = this.jdbcTemplate.update(sql, messageId, sender, receiver);

    if (row <= 0) {

      // no exist
      String insertSQL =
          "insert into im_session_records(session_name,create_time,session_type,sender,receiver,least_message_id) values (?,NOW(),?,?,?,?)";

      this.jdbcTemplate.update(insertSQL, sender, 0, sender, receiver, messageId);
    }

    // 判断反向存在不存在
    String checkSQL =
        "select count(0) as rowCount from im_session_records where session_type = 0 and sender = ? and receiver = ? ";
    Integer rrow =
        this.jdbcTemplate.queryForObject(checkSQL, new Object[] {receiver, sender}, Integer.class);
    if (rrow <= 0) {
      // 反向添加一条记录
      String insertSQL =
          "insert into im_session_records(session_name,create_time,session_type,sender,receiver,least_message_id) values (?,NOW(),?,?,?,?)";

      this.jdbcTemplate.update(insertSQL, receiver, 0, receiver, sender, messageId);
    }
  }

  @Override
  public void batchUpdateMessageNotify(
      List<MessageNotify> singleNotifies, List<MessageNotify> groupNotifies) {
    if (singleNotifies.size() > 0) {

      String sql =
          "update im_session_records set has_new_message = 1 where sender = ? and receiver = ? ";

      List<Object[]> params = Lists.newArrayList();
      for (MessageNotify singleNotify : singleNotifies) {
        params.add(new Object[] {singleNotify.getSender(), singleNotify.getReceiver()});
      }

      this.jdbcTemplate.batchUpdate(sql, params);
    }

    if (groupNotifies.size() > 0) {
      String sql =
          "update im_session_records set has_new_message = 1 where group_name = ? and receiver = ? ";

      List<Object[]> params = Lists.newArrayList();
      for (MessageNotify notify : groupNotifies) {
        params.add(new Object[] {notify.getSender(), notify.getReceiver()});
      }

      this.jdbcTemplate.batchUpdate(sql, params);
    }
  }
}
