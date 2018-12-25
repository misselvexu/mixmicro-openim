package com.acmedcare.framework.newim.server.mq.processor;

import static com.acmedcare.framework.newim.server.mq.MQContext.CLIENT_SESSION_ATTRIBUTE_KEY;

import com.acmedcare.framework.aorp.beans.Principal;
import com.acmedcare.framework.aorp.client.AorpClient;
import com.acmedcare.framework.aorp.exception.InvalidTokenException;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.Message.InnerType;
import com.acmedcare.framework.newim.Message.MQMessage;
import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.Topic;
import com.acmedcare.framework.newim.server.IdService;
import com.acmedcare.framework.newim.server.mq.MQCommand.Common;
import com.acmedcare.framework.newim.server.mq.MQCommand.MonitorClient;
import com.acmedcare.framework.newim.server.mq.MQCommand.SamplingClient;
import com.acmedcare.framework.newim.server.mq.MQContext;
import com.acmedcare.framework.newim.server.mq.MQContext.ClientSession;
import com.acmedcare.framework.newim.server.mq.exception.UnRegisterChannelException;
import com.acmedcare.framework.newim.server.mq.processor.body.TopicSubscribeMapping;
import com.acmedcare.framework.newim.server.mq.processor.header.FixTopicMessageListHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.NewTopicHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.PullTopicListHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.PullTopicSubscribedMappingsHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.RegisterHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.SendTopicMessageHeader;
import com.acmedcare.framework.newim.server.mq.processor.header.SubscribeTopicOperateHeader;
import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.framework.newim.spi.util.Assert;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

/**
 * MQ Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
public class MQProcessor implements NettyRequestProcessor {

  private static final Logger logger = LoggerFactory.getLogger(MQProcessor.class);

  private final MQContext context;
  private final MQService mqService;
  private final AorpClient aorpClient;
  private final IdService idService;

  public MQProcessor(
      MQContext context, MQService mqService, AorpClient aorpClient, IdService idService) {
    this.mqService = mqService;
    this.context = context;
    this.aorpClient = aorpClient;
    this.idService = idService;
  }

  @Override
  public RemotingCommand processRequest(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    logger.info(
        "[MQServer] request code: 0x{} , {}",
        Integer.toHexString(remotingCommand.getCode()),
        remotingCommand.toString());

    RemotingCommand defaultResponse =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), "DEFAULT-RESPONSE");

    try {

      // parse code
      int code = remotingCommand.getCode();
      switch (code) {
          // monitor client biz code
        case MonitorClient.HANDSHAKE:
          // handshake request type recommended: oneway
          break;
        case MonitorClient.REGISTER:
          return this.monitorClientRegister(channelHandlerContext, remotingCommand);
        case MonitorClient.SHUTDOWN:
          return this.monitorClientShutdown(channelHandlerContext, remotingCommand);
        case MonitorClient.TOPIC_SUBSCRIBE:
          return this.monitorClientTopicSubscribe(channelHandlerContext, remotingCommand);
        case MonitorClient.REVOKE_TOPIC_SUBSCRIBE:
          return this.monitorClientRevokeTopicSubscribe(channelHandlerContext, remotingCommand);
        case MonitorClient.FIX_MESSAGE:
          return this.monitorClientFixMessages(channelHandlerContext, remotingCommand);

          // sampling client biz code
        case SamplingClient.HANDSHAKE:
          // handshake request type recommended: oneway
          break;
        case SamplingClient.REGISTER:
          return this.samplingClientRegister(channelHandlerContext, remotingCommand);
        case SamplingClient.SHUTDOWN:
          return this.samplingClientShutdown(channelHandlerContext, remotingCommand);
        case SamplingClient.PULL_TOPIC_SUBSCRIBE_MAPPING:
          return this.samplingClientPullTopicSubscribeMapping(
              channelHandlerContext, remotingCommand);

          // common biz command
        case Common.CREATE_TOPIC:
          return this.newTopic(channelHandlerContext, remotingCommand);
        case Common.PULL_TOPICS:
          return this.pullTopicsList(channelHandlerContext, remotingCommand);
        case Common.TOPIC_MESSAGE_PUSH:
          return this.pushTopicMessage(channelHandlerContext, remotingCommand);
          // no processor c8410635
        default:
          defaultResponse.setBody(
              BizResult.builder()
                  .code(-1)
                  .exception(
                      ExceptionWrapper.builder()
                          .message("unknown biz code : 0x" + Integer.toHexString(code))
                          .build())
                  .build()
                  .bytes());
      }

    } catch (Exception e) {
      logger.warn("[MQServer] request processor exception", e);
    }
    return defaultResponse;
  }

  @Override
  public boolean rejectRequest() {
    return false;
  }

  private Principal validateToken(String token, String passportId, String passport) {
    // check accessToken
    boolean result = this.aorpClient.validateVaguely(token, 2000);
    if (!result) {
      throw new InvalidTokenException("登录票据授权校验失败,无效Token");
    }

    Principal principal = this.aorpClient.getPrincipal(token);
    if (!StringUtils.equals(passportId, principal.getPassportUid().toString())
        || !StringUtils.equalsIgnoreCase(passport, principal.getPassportAccount())) {
      throw new InvalidTokenException("登录票据与通行证不匹配,非法Token");
    }
    return principal;
  }

  private ClientSession validateChannel(Channel channel)
      throws RemotingConnectException, UnRegisterChannelException {
    if (channel == null || !channel.isOpen()) {
      throw new RemotingConnectException("remoting connection is closed or invalid.");
    }

    if (!channel.hasAttr(CLIENT_SESSION_ATTRIBUTE_KEY)) {
      throw new UnRegisterChannelException("remoting connection is un-registed.");
    }

    return channel.attr(CLIENT_SESSION_ATTRIBUTE_KEY).get();
  }

  private RemotingCommand monitorClientRegister(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws RemotingCommandException {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    RegisterHeader header =
        (RegisterHeader) remotingCommand.decodeCommandCustomHeader(RegisterHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    Principal principal =
        validateToken(
            header.getAccessToken(), header.getPassportId().toString(), header.getPassport());

    ClientSession clientSession = new ClientSession();
    BeanUtils.copyProperties(principal, clientSession);
    clientSession.setAreaNo(header.getAreaNo());
    clientSession.setDeviceId(header.getDeviceId());
    clientSession.setOrgId(header.getOrgId());
    clientSession.setNamespace(header.getNamespace());

    // set session info
    channelHandlerContext.channel().attr(CLIENT_SESSION_ATTRIBUTE_KEY).set(clientSession);

    this.context.registerMonitorClient(channelHandlerContext.channel(), clientSession);

    // return success
    response.setBody(BizResult.SUCCESS.bytes());

    return response;
  }

  private RemotingCommand monitorClientShutdown(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {
      ClientSession clientSession = validateChannel(channelHandlerContext.channel());
      this.context.unRegisterMonitorClient(channelHandlerContext.channel(), clientSession);
    } catch (Exception ignore) {
      logger.warn(
          "[IGNORE] un-register monitor client exception , maybe channel already close by client.");
    }
    // return success
    response.setBody(BizResult.SUCCESS.bytes());

    return response;
  }

  private RemotingCommand monitorClientTopicSubscribe(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws UnRegisterChannelException, RemotingConnectException, RemotingCommandException {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    SubscribeTopicOperateHeader header =
        (SubscribeTopicOperateHeader)
            remotingCommand.decodeCommandCustomHeader(SubscribeTopicOperateHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    this.mqService.subscribeTopics(
        header.getNamespace(), header.getPassportId(), header.getPassport(), header.getTopicIds());

    // return success
    response.setBody(BizResult.builder().code(0).build().bytes());

    return response;
  }

  private RemotingCommand monitorClientRevokeTopicSubscribe(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws RemotingCommandException, UnRegisterChannelException, RemotingConnectException {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    SubscribeTopicOperateHeader header =
        (SubscribeTopicOperateHeader)
            remotingCommand.decodeCommandCustomHeader(SubscribeTopicOperateHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    this.mqService.unSubscribeTopics(
        header.getNamespace(), header.getPassportId(), header.getPassport(), header.getTopicIds());

    // return success
    response.setBody(BizResult.builder().code(0).build().bytes());

    return response;
  }

  private RemotingCommand monitorClientFixMessages(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    FixTopicMessageListHeader header =
        (FixTopicMessageListHeader)
            remotingCommand.decodeCommandCustomHeader(FixTopicMessageListHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    List<MQMessage> messages =
        this.mqService.queryMessageList(
            header.getNamespace(),
            header.getLastTopicMessageId(),
            header.getLimit(),
            header.getTopicId());

    // return success
    response.setBody(BizResult.builder().code(0).data(messages).build().bytes());

    return response;
  }

  private RemotingCommand samplingClientRegister(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws RemotingCommandException {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    RegisterHeader header =
        (RegisterHeader) remotingCommand.decodeCommandCustomHeader(RegisterHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    Principal principal =
        validateToken(
            header.getAccessToken(), header.getPassportId().toString(), header.getPassport());

    ClientSession clientSession = new ClientSession();
    BeanUtils.copyProperties(principal, clientSession);
    clientSession.setAreaNo(header.getAreaNo());
    clientSession.setDeviceId(header.getDeviceId());
    clientSession.setOrgId(header.getOrgId());
    clientSession.setNamespace(header.getNamespace());

    // set session info
    channelHandlerContext.channel().attr(CLIENT_SESSION_ATTRIBUTE_KEY).set(clientSession);

    this.context.registerSamplingClient(channelHandlerContext.channel(), clientSession);

    // return success
    response.setBody(BizResult.SUCCESS.bytes());

    return response;
  }

  private RemotingCommand samplingClientShutdown(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      ClientSession clientSession = validateChannel(channelHandlerContext.channel());
      this.context.unRegisterSamplingClient(channelHandlerContext.channel(), clientSession);
    } catch (Exception ignore) {
      logger.warn(
          "[IGNORE] un-register sampling client exception , maybe channel already close by client.");
    }
    // return success
    response.setBody(BizResult.SUCCESS.bytes());

    return response;
  }

  private RemotingCommand samplingClientPullTopicSubscribeMapping(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws RemotingCommandException, UnRegisterChannelException, RemotingConnectException {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    PullTopicSubscribedMappingsHeader header =
        (PullTopicSubscribedMappingsHeader)
            remotingCommand.decodeCommandCustomHeader(PullTopicSubscribedMappingsHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    TopicSubscribeMapping result =
        this.mqService.pullTopicSubscribedMapping(
            header.getNamespace(),
            Long.parseLong(header.getTopicId()),
            header.getPassportId(),
            header.getPassport());

    // return success
    response.setBody(BizResult.builder().code(0).data(result).build().bytes());

    return response;
  }

  private RemotingCommand newTopic(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws UnRegisterChannelException, RemotingConnectException, RemotingCommandException {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    NewTopicHeader header =
        (NewTopicHeader) remotingCommand.decodeCommandCustomHeader(NewTopicHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    Topic topic = new Topic();
    topic.setTopicTag(header.getTopicTag());
    topic.setTopicName(header.getTopicName());
    topic.setTopicExt(header.getTopicExt());
    topic.setTopicDesc(header.getTopicDesc());
    topic.setNamespace(header.getNamespace());

    Long[] ids = this.mqService.createNewTopic(topic);

    // return success
    response.setBody(BizResult.builder().code(0).data(ids[0]).build().bytes());

    return response;
  }

  private RemotingCommand pushTopicMessage(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws Exception {
    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    SendTopicMessageHeader header =
        (SendTopicMessageHeader)
            remotingCommand.decodeCommandCustomHeader(SendTopicMessageHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    MQMessage mqMessage = new MQMessage();
    mqMessage.setTopicId(header.getTopicId());
    mqMessage.setTopicTag(header.getTopicTag());
    mqMessage.setBody(remotingCommand.getBody());
    mqMessage.setMessageType(MessageType.MQ);
    mqMessage.setInnerType(InnerType.NORMAL);
    mqMessage.setSender(header.getPassportId());
    mqMessage.setMid(idService.nextId());

    this.mqService.broadcastTopicMessages(context, mqMessage);

    // return success
    response.setBody(BizResult.builder().code(0).data(mqMessage.getMid()).build().bytes());
    return response;
  }

  private RemotingCommand pullTopicsList(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand)
      throws UnRegisterChannelException, RemotingConnectException, RemotingCommandException {

    ClientSession clientSession = validateChannel(channelHandlerContext.channel());

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    PullTopicListHeader header =
        (PullTopicListHeader) remotingCommand.decodeCommandCustomHeader(PullTopicListHeader.class);

    Assert.notNull(header, "Request header must not be null.");

    List<Topic> topics = this.mqService.pullTopicsList(header.getNamespace());

    // return success
    response.setBody(BizResult.builder().code(0).data(topics).build().bytes());

    return response;
  }
}
