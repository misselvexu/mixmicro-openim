package com.acmedcare.microservices.im.core;

import com.acmedcare.microservices.im.RemotingApplication.Datas;
import com.acmedcare.microservices.im.biz.BizCode;
import com.acmedcare.microservices.im.biz.bean.Account;
import com.acmedcare.microservices.im.biz.bean.Message;
import com.acmedcare.microservices.im.biz.bean.Message.GroupMessage;
import com.acmedcare.microservices.im.biz.bean.Message.InnerType;
import com.acmedcare.microservices.im.biz.bean.Message.MessageType;
import com.acmedcare.microservices.im.biz.bean.Message.SingleMessage;
import com.acmedcare.microservices.im.biz.request.ServerPushMessageHeader;
import com.acmedcare.microservices.im.kits.DefaultThreadFactory;
import com.acmedcare.microservices.im.kits.ThreadKit;
import com.acmedcare.microservices.im.kits.ThreadKit.WrapExceptionRunnable;
import com.acmedcare.tiffany.framework.remoting.common.RemotingHelper;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server Facade For Export Api
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
public final class ServerFacade {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerFacade.class);
  private static final ExecutorService DEFAULT_ASYNC_EXECUTOR =
      new ThreadPoolExecutor(
          8,
          32,
          60L,
          TimeUnit.SECONDS,
          new SynchronousQueue<Runnable>(),
          new DefaultThreadFactory("default-async-executor"));
  private static TiffanySocketServer server;

  public static void init(TiffanySocketServer server) {
    ServerFacade.server = server;
  }

  /**
   * Get Channels Mapping
   *
   * @return channel map
   */
  public static Map<String, List<Channel>> channelsMapping() {
    return ChannelCaches.DEFAULT_CHANNELS_MAPPING;
  }

  /** Clear Caches Channels */
  public static void scheduleCleanCaches() {
    for (Entry<String, List<Channel>> entry : channelsMapping().entrySet()) {
      List<Channel> clientChannels = entry.getValue();
      Iterator<Channel> channelIterator = clientChannels.iterator();
      while (channelIterator.hasNext()) {
        Channel channel = channelIterator.next();
        if (channel != null) {
          System.out.println(
              entry.getKey() + " -> " + RemotingHelper.parseChannelRemoteAddr(channel));
          if (!channel.isOpen() || !channel.isActive() || !channel.isWritable()) {
            channelIterator.remove();
            System.out.println("移除 channel: " + RemotingHelper.parseChannelRemoteAddr(channel));
            try {
              channel.close();
            } catch (Exception ignore) {
            }
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
    ServerFacade.DEFAULT_ASYNC_EXECUTOR.execute(runnable);
  }

  public static void destory() {
    ThreadKit.gracefulShutdown(DEFAULT_ASYNC_EXECUTOR, 10, 10, TimeUnit.SECONDS);
  }

  /** Channel Caches Local */
  public static class ChannelCaches {

    /** DEFAULT passport-channel holder */
    private static final Map<String, List<Channel>> DEFAULT_CHANNELS_MAPPING =
        Maps.newConcurrentMap();
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @EqualsAndHashCode
  public static class MessageNotify implements Serializable {
    private static final long serialVersionUID = -6310126114617861063L;
    private String sender;
    private String receiver;

    @Builder
    public MessageNotify(String sender, String receiver) {
      this.sender = sender;
      this.receiver = receiver;
    }
  }

  /** Executor for other service , like http */
  public static class Executor {

    static void processSession(final List<Message> messages) {

      final List<MessageNotify> singleNotifies = Lists.newArrayList();
      final List<MessageNotify> groupNotifies = Lists.newArrayList();

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

              singleNotifies.add(
                  MessageNotify.builder()
                      .sender(singleMessage.getSender())
                      .receiver(singleMessage.getReceiver())
                      .build());
              //
              Datas.persistenceExecutor.saveOrUpdateSingleSessionRecord(
                  singleMessage.getSender(), singleMessage.getReceiver(), singleMessage.getMid());

              break;
            case GROUP:
              GroupMessage groupMessage = (GroupMessage) message;
              if (InnerType.COMMAND.equals(groupMessage.getInnerType())) {
                continue;
              }

              String sender = groupMessage.getSender();
              List<Account> groupReceivers =
                  Datas.persistenceExecutor.queryGroupMembers(groupMessage.getGroup());

              for (Account groupReceiver : groupReceivers) {
                groupNotifies.add(
                    MessageNotify.builder()
                        .sender(groupMessage.getGroup())
                        .receiver(groupReceiver.getUsername())
                        .build());
              }

              Datas.persistenceExecutor.saveOrUpdateGroupSessionRecord(
                  groupMessage.getGroup(), groupMessage.getMid(), groupReceivers);
              break;
          }

          // sync update notify
          submitTask(
              new WrapExceptionRunnable(
                  new Runnable() {
                    @Override
                    public void run() {
                      Datas.persistenceExecutor.batchUpdateMessageNotify(
                          singleNotifies, groupNotifies);
                    }
                  }));
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
                  Set<Long> batchMessageBugIds = Sets.newHashSet();
                  List<Object[]> saveMessageSendParams = Lists.newArrayList();
                  List<Object[]> saveMessageReceiveParams = Lists.newArrayList();

                  // 群队列
                  String groupName = null;
                  List<GroupMessage> groupMessageList = Lists.newArrayList();

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

                        List<Account> groupReceivers =
                            Datas.persistenceExecutor.queryGroupMembers(groupName);

                        for (Account groupReceiver : groupReceivers) {
                          // message_id,sender,receiver,group_id
                          saveMessageReceiveParams.add(
                              new Object[] {
                                groupMessage.getMid(),
                                groupMessage.getSender(),
                                groupReceiver.getUsername(),
                                groupMessage.getGroup()
                              });
                        }

                        break;

                        // 单聊消息
                      case SINGLE:
                        SingleMessage singleMessage = (SingleMessage) message;
                        singleMessageList.add(singleMessage);

                        if (InnerType.COMMAND.equals(singleMessage.getInnerType())) {
                          continue;
                        }

                        if (batchMessageBugIds.add(singleMessage.getMid())) {

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
                        }

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
                    System.out.println("当前在线用户数:" + channelsMapping().size());
                    //
                    List<Account> groupReceivers =
                        Datas.persistenceExecutor.queryGroupMembers(groupName);

                    if (groupReceivers != null && groupReceivers.size() > 0) {
                      Set<Channel> channels = Sets.newHashSet();
                      for (Account groupReceiver : groupReceivers) {
                        if (channelsMapping().containsKey(groupReceiver.getUsername())) {

                          channels.addAll(channelsMapping().get(groupReceiver.getUsername()));
                          System.out.println("[群发]获取:" + groupReceiver.getUsername() + " ,的远程端口连接");
                        }
                      }

                      ServerPushMessageHeader pushMessageHeader =
                          ServerPushMessageHeader.builder()
                              .messageType(MessageType.GROUP.name())
                              .build();
                      for (GroupMessage groupMessage : groupMessageList) {

                        // build request
                        RemotingCommand command =
                            RemotingCommand.createRequestCommand(
                                BizCode.SERVER_PUSH_MESSAGE, pushMessageHeader);
                        command.setBody(groupMessage.bytes());

                        for (Channel channel : channels) {
                          if (channel != null) {
                            if (channel.isWritable()) {
                              try {

                                channel
                                    .writeAndFlush(command)
                                    .addListener(
                                        new ChannelFutureListener() {
                                          @Override
                                          public void operationComplete(ChannelFuture future)
                                              throws Exception {
                                            if (!future.isSuccess()) {
                                              channel.close();
                                            }
                                          }
                                        });
                                //                                ServerFacade.server
                                //                                    .getServer()
                                //                                    .invokeOneway(channel,
                                // command, 5000);
                                System.out.println(
                                    "[群发]推送消息给客户端:"
                                        + RemotingHelper.parseChannelRemoteAddr(channel));
                              } catch (Exception e) {
                                LOGGER.error(
                                    "Send Message To Remote:[{}] Failed;",
                                    RemotingHelper.parseChannelRemoteAddr(channel));
                              }
                            }
                          }
                        }
                      }
                    }
                  }

                  if (singleMessageList.size() > 0) {

                    System.out.println("当前在线用户数:" + channelsMapping().size());

                    ServerPushMessageHeader pushMessageHeader =
                        ServerPushMessageHeader.builder()
                            .messageType(MessageType.SINGLE.name())
                            .build();

                    Set<String> messageReceivers = Sets.newHashSet();

                    for (SingleMessage singleMessage : singleMessageList) {
                      messageReceivers.add(singleMessage.getReceiver());
                    }

                    SingleMessage message = singleMessageList.get(0);

                    for (String receiver : messageReceivers) {

                      try {

                        System.out.println("[单聊]准备发送消息给:" + receiver);
                        message.setReceiver(receiver);

                        // build request
                        RemotingCommand command =
                            RemotingCommand.createRequestCommand(
                                BizCode.SERVER_PUSH_MESSAGE, pushMessageHeader);
                        command.setBody(message.bytes());
                        List<Channel> channels = channelsMapping().get(receiver);
                        if (channels != null) {
                          for (Channel channel : channels) {
                            if (channel != null && channel.isWritable()) {
                              try {
                                channel
                                    .writeAndFlush(command)
                                    .addListener(
                                        new ChannelFutureListener() {
                                          @Override
                                          public void operationComplete(ChannelFuture future)
                                              throws Exception {
                                            if (!future.isSuccess()) {
                                              channel.close();
                                            }
                                          }
                                        });
                                // ServerFacade.server.getServer().invokeOneway(channel, command,
                                // 100000);
                                System.out.println(
                                    "[单聊]推送消息给客户端:"
                                        + RemotingHelper.parseChannelRemoteAddr(channel));
                              } catch (Exception e) {
                                LOGGER.error(
                                    "Send Message To Remote:[{}] Failed;",
                                    RemotingHelper.parseChannelRemoteAddr(channel));
                              }
                            }
                          }
                        }
                      } catch (Exception e) {
                        e.printStackTrace();
                      }
                    }
                  }
                }));
      }
    }
  }
}
