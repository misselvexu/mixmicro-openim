package com.acmedcare.framework.newim.server.mq.processor;

import com.acmedcare.framework.aorp.client.AorpClient;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.server.mq.MQCommand.MonitorClient;
import com.acmedcare.framework.newim.server.mq.MQCommand.SamplingClient;
import com.acmedcare.framework.newim.server.mq.MQContext;
import com.acmedcare.framework.newim.server.mq.service.MQService;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  public MQProcessor(MQContext context, MQService mqService, AorpClient aorpClient) {
    this.mqService = mqService;
    this.context = context;
    this.aorpClient = aorpClient;
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
          return this.samplingClientPullTopicSubscibeMapping(
              channelHandlerContext, remotingCommand);

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

  private RemotingCommand monitorClientRegister(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand monitorClientShutdown(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand monitorClientTopicSubscribe(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand monitorClientRevokeTopicSubscribe(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand monitorClientFixMessages(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand samplingClientRegister(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand samplingClientShutdown(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }

  private RemotingCommand samplingClientPullTopicSubscibeMapping(
      ChannelHandlerContext channelHandlerContext, RemotingCommand remotingCommand) {

    // TODO

    return null;
  }
}
