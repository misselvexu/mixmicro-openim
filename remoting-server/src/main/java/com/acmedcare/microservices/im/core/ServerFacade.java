package com.acmedcare.microservices.im.core;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizCode;
import com.acmedcare.microservices.im.biz.bean.Account;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.bean.Message.GroupMessage;
import com.acmedcare.microservices.im.biz.bean.Message.InnerType;
import com.acmedcare.microservices.im.biz.bean.Message.MessageType;
import com.acmedcare.microservices.im.biz.bean.Message.SingleMessage;
import com.acmedcare.microservices.im.biz.request.PushMessageHeader;
import com.acmedcare.microservices.im.kits.DefaultThreadFactory;
import com.acmedcare.microservices.im.kits.ThreadKit;
import com.acmedcare.microservices.im.kits.ThreadKit.WrapExceptionRunnable;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server Facade For Export Api
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
public final class ServerFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerFacade.class);

  private static final ExecutorService DEFAULT_ASYNC_EXECUTOR =
      new ThreadPoolExecutor(
          4,
          Runtime.getRuntime().availableProcessors() * 2,
          60L,
          TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>(),
          new DefaultThreadFactory("default-async-executor"));

  /**
   * Get Channels Mapping
   *
   * @return channel map
   */
  public static Map<Account, List<ClientChannel>> channelsMapping() {
    return ChannelCaches.DEFAULT_CHANNELS_MAPPING;
  }

  /** Clear Caches Channels */
  public static void scheduleCleanCaches() {
    for (Entry<Account, List<ClientChannel>> entry : channelsMapping().entrySet()) {
      List<ClientChannel> clientChannels = entry.getValue();
      Iterator<ClientChannel> iterator = clientChannels.iterator();
      while (iterator.hasNext()) {
        ClientChannel clientChannel = iterator.next();
        if (clientChannel != null) {
          Channel channel = clientChannel.getChannel();
          if (channel == null || !channel.isWritable()) {
            iterator.remove();
          }
        }
      }
    }
  }

  /**
   * Submit Async Task to pool
   *
   * @param runnable runnable task
   */
  public static void submitTask(WrapExceptionRunnable runnable) {
    ServerFacade.DEFAULT_ASYNC_EXECUTOR.submit(runnable);
  }

  public static void destory() {
    ThreadKit.gracefulShutdown(DEFAULT_ASYNC_EXECUTOR, 10, 10, TimeUnit.SECONDS);
  }

  /** Channel Caches Local */
  public static class ChannelCaches {

    /** DEFAULT username-channel holder */
    private static final Map<Account, List<ClientChannel>> DEFAULT_CHANNELS_MAPPING =
        Maps.newConcurrentMap();
  }

  /** Executor for other service , like http */
  public static class Executor {

    static void processSession(final List<Message> messages) {
      if (messages.size() > 0) {

        // 此处消息处理量不是特别大,循环处理
        for (Message message : messages) {
          MessageType messageType = message.getMessageType();
          switch (messageType) {
            case SINGLE:
              SingleMessage singleMessage = (SingleMessage) message;
              if (InnerType.COMMAND.equals(singleMessage.getInnerType())) {
                continue;
              }
              //
              Datas.persistenceExecutor.saveOrUpdateSingleSessionRecord(
                  singleMessage.getSender(), singleMessage.getReceiver(), singleMessage.getMid());

              break;
            case GROUP:
              GroupMessage groupMessage = (GroupMessage) message;
              if (InnerType.COMMAND.equals(groupMessage.getInnerType())) {
                continue;
              }

              List<Account> groupReceivers =
                  Datas.persistenceExecutor.queryGroupMembers(groupMessage.getGroup());

              Datas.persistenceExecutor.saveOrUpdateGroupSessionRecord(
                  groupMessage.getGroup(), groupMessage.getMid(), groupReceivers);
              break;
          }
        }
      }
    }
    /**
     * Send Message
     *
     * @param messages message list
     */
    public static void sendMessageAsync(final List<Message> messages) {

      // check
      if (messages != null && messages.size() > 0) {
        submitTask(
            new WrapExceptionRunnable(
                () -> {

                  // process session
                  processSession(messages);

                  // save params
                  List<Object[]> saveMessageParams = Lists.newArrayList();
                  List<Object[]> saveMessageSendParams = Lists.newArrayList();
                  List<Object[]> saveMessageReceiveParams = Lists.newArrayList();

                  // 群队列
                  String groupName = null;
                  List<GroupMessage> groupMessageList = Lists.newArrayList();

                  Set<String> singleReceivers = Sets.newHashSet();
                  List<SingleMessage> singleMessageList = Lists.newArrayList();

                  // foreach to send message
                  for (Message message : messages) {

                    MessageType messageType = message.getMessageType();
                    // 判断类型
                    switch (messageType) {

                        // 群组消息
                      case GROUP:
                        GroupMessage groupMessage = (GroupMessage) message;
                        groupMessageList.add(groupMessage);

                        // build send queue
                        if (groupName == null) {
                          groupName = groupMessage.getGroup();
                        }

                        if (InnerType.COMMAND.equals(groupMessage.getInnerType())) {
                          continue;
                        }
                        // 群组
                        // message_id,message_content,sender,message_type,receive_type,send_timestamp,receiver,receiver_group
                        saveMessageParams.add(
                            new Object[] {
                              groupMessage.getMid(),
                              new String(groupMessage.getBody()),
                              groupMessage.getSender(),
                              1,
                              groupMessage.getMessageType().name(),
                              null,
                              groupMessage.getGroup()
                            });

                        // message_id,sender,receiver,group_id
                        saveMessageSendParams.add(
                            new Object[] {
                              groupMessage.getMid(),
                              groupMessage.getSender(),
                              null,
                              groupMessage.getGroup()
                            });

                        // message_id,sender,receiver,group_id
                        saveMessageReceiveParams.add(
                            new Object[] {
                              groupMessage.getMid(),
                              groupMessage.getSender(),
                              null,
                              groupMessage.getGroup()
                            });

                        break;

                        // 单聊消息
                      case SINGLE:
                        SingleMessage singleMessage = (SingleMessage) message;
                        singleMessageList.add(singleMessage);

                        singleReceivers.add(singleMessage.getReceiver());

                        if (InnerType.COMMAND.equals(singleMessage.getInnerType())) {
                          continue;
                        }

                        saveMessageParams.add(
                            new Object[] {
                              singleMessage.getMid(),
                              new String(singleMessage.getBody()),
                              singleMessage.getSender(),
                              0,
                              singleMessage.getMessageType().name(),
                              singleMessage.getReceiver(),
                              null
                            });

                        saveMessageSendParams.add(
                            new Object[] {
                              singleMessage.getMid(),
                              singleMessage.getSender(),
                              singleMessage.getReceiver(),
                              null
                            });

                        saveMessageReceiveParams.add(
                            new Object[] {
                              singleMessage.getMid(),
                              singleMessage.getSender(),
                              singleMessage.getReceiver(),
                              null
                            });

                        break;
                    }
                  }

                  // 1. 批量存储消息
                  // 2. 批量存储发送
                  // 3. 批量存储接收
                  Datas.persistenceExecutor.saveMessage(
                      saveMessageParams, saveMessageSendParams, saveMessageReceiveParams);

                  // push
                  if (groupMessageList.size() > 0) {
                    //
                    List<Account> groupReceivers =
                        Datas.persistenceExecutor.queryGroupMembers(groupName);

                    if (groupReceivers != null && groupReceivers.size() > 0) {
                      List<ClientChannel> channels = Lists.newArrayList();
                      for (Account groupReceiver : groupReceivers) {
                        if (channelsMapping().containsKey(groupReceiver)) {
                          channels.addAll(channelsMapping().get(groupReceiver));
                          System.out.println("[群发]获取:" + groupReceiver.getUsername() + " ,的远程端口连接");
                        }
                      }

                      PushMessageHeader pushMessageHeader =
                          PushMessageHeader.builder().messageType(MessageType.GROUP.name()).build();
                      for (GroupMessage groupMessage : groupMessageList) {

                        // build request
                        RemotingCommand command =
                            RemotingCommand.createRequestCommand(
                                BizCode.SERVER_PUSH_MESSAGE, pushMessageHeader);
                        command.setBody(groupMessage.bytes());

                        for (ClientChannel channel : channels) {
                          if (channel != null) {
                            if (channel.getChannel() != null) {
                              if (channel.getChannel().isWritable()) {
                                try {
                                  Channel c = channel.getChannel();
                                  c.writeAndFlush(command);
                                  System.out.println(
                                      "[群发]推送消息给客户端:" + RemotingHelper.parseChannelRemoteAddr(c));
                                } catch (Exception e) {
                                  LOGGER.error(
                                      "Send Message To Remote:[{}] Failed;",
                                      RemotingHelper.parseChannelRemoteAddr(channel.getChannel()));
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }

                  if (singleMessageList.size() > 0) {

                    List<ClientChannel> channels = Lists.newArrayList();
                    for (String singleReceiver : singleReceivers) {
                      Account rtemp = Account.builder().username(singleReceiver).build();
                      if (channelsMapping().containsKey(rtemp)) {
                        channels.addAll(channelsMapping().get(rtemp));
                        System.out.println("[单聊]获取:" + singleReceiver + " ,的远程端口连接");
                      }
                    }

                    PushMessageHeader pushMessageHeader =
                        PushMessageHeader.builder().messageType(MessageType.SINGLE.name()).build();
                    for (SingleMessage singleMessage : singleMessageList) {

                      // build request
                      RemotingCommand command =
                          RemotingCommand.createRequestCommand(
                              BizCode.SERVER_PUSH_MESSAGE, pushMessageHeader);
                      command.setBody(singleMessage.bytes());

                      for (ClientChannel channel : channels) {
                        if (channel != null) {
                          if (channel.getChannel() != null) {
                            if (channel.getChannel().isWritable()) {
                              try {
                                Channel c = channel.getChannel();
                                c.writeAndFlush(command);
                                System.out.println(
                                    "[单聊]推送消息给客户端:" + RemotingHelper.parseChannelRemoteAddr(c));
                              } catch (Exception e) {
                                LOGGER.error(
                                    "Send Message To Remote:[{}] Failed;",
                                    RemotingHelper.parseChannelRemoteAddr(channel.getChannel()));
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }));
      }
    }
  }
}
