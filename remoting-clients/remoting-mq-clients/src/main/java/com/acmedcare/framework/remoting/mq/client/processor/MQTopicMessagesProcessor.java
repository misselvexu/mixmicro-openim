package com.acmedcare.framework.remoting.mq.client.processor;

import com.acmedcare.framework.remoting.mq.client.AcmedcareLogger;
import com.acmedcare.framework.remoting.mq.client.AcmedcareMQRemoting;
import com.acmedcare.framework.remoting.mq.client.biz.BizResult;
import com.acmedcare.tiffany.framework.remoting.android.HandlerMessageListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;

/**
 * MQTopicMessagesProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
public class MQTopicMessagesProcessor implements XLMRRequestProcessor<Void, Void> {

  private final AcmedcareMQRemoting acmedcareMQRemoting;

  public MQTopicMessagesProcessor(AcmedcareMQRemoting acmedcareMQRemoting) {
    this.acmedcareMQRemoting = acmedcareMQRemoting;
  }

  @Override
  public RemotingCommand processRequest(
      IoSession ioSession,
      RemotingCommand remotingCommand,
      HandlerMessageListener<Void, Void> handlerMessageListener)
      throws Exception {

    RemotingCommand response =
        RemotingCommand.createResponseCommand(remotingCommand.getCode(), null);

    try {

      // TODO process mq topic message



    } catch (Exception e) {
      AcmedcareLogger.e(null, e, "Parse Received MQ Server Message failed ");
      //noinspection unchecked
      response.setBody(
          BizResult.builder()
              .code(-1)
              .exception(BizResult.ExceptionWrapper.builder().message(e.getMessage()).build())
              .build()
              .bytes());
    }
    return response;
  }
}
