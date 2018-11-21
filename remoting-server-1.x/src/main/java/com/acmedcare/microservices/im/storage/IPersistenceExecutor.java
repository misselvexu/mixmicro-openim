package com.acmedcare.microservices.im.storage;

import com.acmedcare.microservices.im.biz.bean.Account;
import com.acmedcare.microservices.im.biz.bean.Group;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.bean.Session;
import com.acmedcare.microservices.im.biz.request.PushMessageStatusHeader.PMT;
import com.acmedcare.microservices.im.core.ServerFacade.MessageNotify;
import com.acmedcare.microservices.im.exception.DataAccessException;
import java.util.List;
import java.util.Set;

/**
 * Persistence Handler
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
public interface IPersistenceExecutor {

  /**
   * Save Message
   *
   * @param messages msg
   */
  void saveMessage(
      List<Object[]> messages, List<Object[]> messageSenders, List<Object[]> messageReceivers);

  /**
   * Query Account Groups
   *
   * @param username passport
   * @return list
   */
  List<Group> queryAccountGroups(String username);

  /**
   * Query Account Group Message List
   *
   * @param username passport
   * @param sender group flag
   * @param leastMessageId least message sender
   * @param limit limit size
   * @return list
   */
  List<Message> queryAccountGroupMessages(
      String username, String sender, int type, long leastMessageId, long limit)
      throws DataAccessException;

  /**
   * save or update message read status
   *
   * @param username passport
   * @param pmt push message innerType
   * @param sender group code or passport
   * @param leastMessageId least message sender
   * @return result
   */
  boolean saveOrUpdateMessageReadStatus(
      String username, PMT pmt, String sender, long leastMessageId);

  /**
   * Create New Room
   *
   * @param groupName
   * @param members
   */
  void newGroup(String groupId,String groupName, String owner, Set<String> members);

  void addGroupMember(String groupId, Set<String> members) throws DataAccessException;

  void deleteGroupMember(String groupId, List<String> members);

  List<Session> queryAccountSessions(String username);

  List<Account> queryGroupMembers(String group);

  /**
   * @param username
   * @param type
   * @param flagId
   * @return
   */
  Session queryAccountSessionStatus(String username, int type, String flagId) throws DataAccessException;

  void saveOrUpdateSingleSessionRecord(String sender, String receiver, Long messageId);

  void saveOrUpdateGroupSessionRecord(String group, Long mid, List<Account> groupReceivers);

  void batchUpdateMessageNotify(List<MessageNotify> singleNotifies, List<MessageNotify> groupNotifies);
}
