package com.acmedcare.framework.newim.server.endpoint.schedule;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.boot.web.socket.processor.WssSession;
import com.acmedcare.framework.kits.executor.AsyncRuntimeExecutor;
import com.acmedcare.framework.kits.thread.DefaultThreadFactory;
import com.acmedcare.framework.newim.GroupMemberRef;
import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.client.MessageAttribute;
import com.acmedcare.framework.newim.server.RemotingWssServer;
import com.acmedcare.framework.newim.server.core.IMSession;
import com.acmedcare.framework.newim.server.core.SessionContextConstants.RemotePrincipal;
import com.acmedcare.framework.newim.server.endpoint.WssSessionContext;
import com.acmedcare.framework.newim.storage.api.GroupRepository;
import com.acmedcare.framework.newim.storage.api.MessageRepository;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.DisposableBean;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.acmedcare.framework.newim.server.ClusterLogger.wssServerLog;

/**
 * Schedule System Session Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public class ScheduleSysContext extends WssSessionContext implements DisposableBean {

  /** 保存调度站点的登录情况 */
  private static Map<ScheduleWssClientInstance, ScheduleWssClientAccountInstance>
      remotingWssScheduleInstances = Maps.newConcurrentMap();

  private static ExecutorService asyncProcessExecutor =
      new ThreadPoolExecutor(
          4,
          8,
          5000,
          TimeUnit.SECONDS,
          new LinkedBlockingQueue<>(64),
          new DefaultThreadFactory("schedule-async-process-executor"),
          new CallerRunsPolicy());

  private final GroupRepository groupRepository;
  private final MessageRepository messageRepository;

  public ScheduleSysContext(
      IMSession imSession, GroupRepository groupRepository, MessageRepository messageRepository) {
    super(imSession);
    this.groupRepository = groupRepository;
    this.messageRepository = messageRepository;
  }

  /**
   * 根据父机构编号删选出子机构列表
   *
   * @param parentOrgId 父机构编号
   * @return 子机构列表
   */
  public List<ScheduleWssClientInstance> querySubOrgs(String parentOrgId) {
    return remotingWssScheduleInstances.keySet().stream()
        .filter(instance -> Objects.equals(parentOrgId, instance.getParentOrgId()))
        .collect(Collectors.toList());
  }
  /**
   * Register Login-ed Wss Client
   *
   * @param principal principal detail
   * @param session session channel
   */
  @Override
  public void registerWssClient(RemotePrincipal principal, WssSession session) {
    super.registerWssClient(principal, session);

    // schedule sys register wss client

  }

  @Override
  public void revokeWssClient(WssSession session) {
    super.revokeWssClient(session);

    // schedule sys revoke wss client

  }

  /**
   * Invoked by a BeanFactory on destruction of a singleton.
   *
   * @throws Exception in case of shutdown errors. Exceptions will get logged but not rethrown to
   *     allow other beans to release their resources too.
   */
  @Override
  public void destroy() throws Exception {}

  public void register(
      Principal principal, String aresNo, String orgId, String orgName, String parentOrgId) {
    ScheduleWssClientInstance instance =
        ScheduleWssClientInstance.builder()
            .areaNo(aresNo)
            .orgId(orgId)
            .orgName(orgName)
            .parentOrgId(parentOrgId)
            .build();
    if (remotingWssScheduleInstances.containsKey(instance)) {
      remotingWssScheduleInstances
          .get(instance)
          .getPrincipals()
          .put(principal.getPassportUid(), principal);
    } else {
      Map<Long, Principal> map = Maps.newHashMap();
      map.put(principal.getPassportUid(), principal);
      ScheduleWssClientAccountInstance accountInstance =
          ScheduleWssClientAccountInstance.builder()
              .scheduleWssClientInstance(instance)
              .principals(map)
              .build();
      remotingWssScheduleInstances.put(instance, accountInstance);
    }
  }

  public void revoke(Principal principal, String areaNo, String orgId) {
    ScheduleWssClientInstance instance =
        ScheduleWssClientInstance.builder().areaNo(areaNo).orgId(orgId).build();

    if (remotingWssScheduleInstances.containsKey(instance)) {
      remotingWssScheduleInstances.get(instance).getPrincipals().remove(principal.getPassportUid());
    }
  }

  public void pushMessage(String namespace, String areaNo, String subOrgId, String orderDetail) {
    ScheduleWssClientInstance instance =
        ScheduleWssClientInstance.builder().areaNo(areaNo).orgId(subOrgId).build();

    if (remotingWssScheduleInstances.containsKey(instance)) {
      // accounts
      Map<Long, Principal> accounts = remotingWssScheduleInstances.get(instance).getPrincipals();
      // channel
      List<WssSession> sessions = Lists.newArrayList();
      List<String> passports = Lists.newArrayList();
      accounts.forEach(
          (key, value) -> {
            sessions.add(getLocalSession(key).getObject2());
            passports.add(key.toString());
          });
      // sessions
      sessions.forEach(
          session ->
              asyncProcessExecutor.execute(
                  () -> {
                    session.sendText(orderDetail);
                    wssServerLog.info(
                        "[WSS] async send message:{} to session : {} succeed.",
                        orderDetail,
                        RemotingHelper.parseChannelRemoteAddr(session.channel()));
                  }));

      // push to tcp
      forwardMessage(namespace, passports, orderDetail);
    }
  }

  public long pushMessage(
      String namespace,
      String areaNo,
      String message,
      String receiver,
      String sender,
      Message.MessageType type)
      throws UnsupportedEncodingException {

    long mid = RemotingWssServer.Ids.snowflake.nextId();

    AsyncRuntimeExecutor.getAsyncThreadPool()
        .execute(
            () -> {
              try {
                switch (type) {
                  case SINGLE:
                    Message.SingleMessage singleMessage = new Message.SingleMessage();
                    singleMessage.setReadFlag(false);
                    singleMessage.setReceiver(receiver);
                    singleMessage.setBody(message.getBytes("UTF-8"));
                    singleMessage.setSender(sender);
                    singleMessage.setMid(mid);
                    singleMessage.setPersistent(true);
                    singleMessage.setMessageType(Message.MessageType.SINGLE);
                    singleMessage.setInnerType(Message.InnerType.NORMAL);

                    messageRepository.saveMessage(singleMessage);

                    // 发送消息到服务器
                    imSession.sendMessageToPassport(
                        namespace, receiver, type, singleMessage.bytes());

                    // 分发消息到其他服务器
                    MessageAttribute attribute = MessageAttribute.builder().build();
                    imSession.distributeMessage(attribute, singleMessage);

                    break;

                  case GROUP:

                    // query group member list
                    List<GroupMemberRef> refs =
                        this.groupRepository.queryGroupMembers(namespace, receiver);
                    List<String> receivers = Lists.newArrayList();
                    for (GroupMemberRef ref : refs) {
                      receivers.add(ref.getMemberId());
                    }

                    Message.GroupMessage groupMessage = new Message.GroupMessage();
                    groupMessage.setBody(message.getBytes("UTF-8"));
                    groupMessage.setSender(sender);
                    groupMessage.setGroup(receiver);
                    groupMessage.setReceivers(receivers);
                    groupMessage.setMid(mid);
                    groupMessage.setPersistent(true);
                    groupMessage.setMessageType(Message.MessageType.GROUP);
                    groupMessage.setInnerType(Message.InnerType.NORMAL);

                    messageRepository.saveMessage(groupMessage);

                    groupMessage.setReceivers(Lists.newArrayList());
                    // 发送消息到服务器
                    for (String receiverId : receivers) {
                      System.out.println("发送消息给客户端:" + receiverId);
                      imSession.sendMessageToPassport(
                          namespace, receiverId, type, groupMessage.bytes());
                    }

                    // 分发消息到其他服务器
                    attribute = MessageAttribute.builder().build();
                    imSession.distributeMessage(attribute, groupMessage);

                    break;
                  default:
                    break;
                }

              } catch (Exception e) {
                wssServerLog.error("处理WSS转发消息异常", e);
              }
            });

    return mid;
  }
}
