package com.acmedcare.framework.newim.server.mq.processor;

import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.BizResult.ExceptionWrapper;
import com.acmedcare.framework.newim.server.mq.MQCommand.MonitorClient;
import com.acmedcare.framework.newim.server.mq.MQCommand.SamplingClient;
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
        case MonitorClient.REGISTER:
        case MonitorClient.SHUTDOWN:
        case MonitorClient.TOPIC_SUBSCRIBE:
        case MonitorClient.REVOKE_TOPIC_SUBSCRIBE:
        case MonitorClient.FIX_MESSAGE:
          break;

          // sampling client biz code
        case SamplingClient.HANDSHAKE:
        case SamplingClient.REGISTER:
        case SamplingClient.SHUTDOWN:
        case SamplingClient.PULL_TOPIC_SUBSCRIBE_MAPPING:
          break;

          // no processor
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
}
