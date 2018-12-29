package com.acmedcare.framework.remoting.mq.client.processor;

import com.acmedcare.framework.remoting.mq.client.AcmedcareMQRemoting;
import com.acmedcare.framework.remoting.mq.client.biz.BizResult;
import com.acmedcare.framework.remoting.mq.client.events.AcmedcareEvent;
import com.acmedcare.framework.remoting.mq.client.events.AcmedcareEvent.OnTopicSubscribeEmptyEventData;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.acmedcare.tiffany.framework.remoting.android.HandlerMessageListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;
import com.alibaba.fastjson.JSON;

/**
 * OnTopicSubscribedEmptyProcessor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-29.
 */
public class OnTopicSubscribedEmptyProcessor implements XLMRRequestProcessor<Void, Void> {
  private final AcmedcareMQRemoting acmedcareMQRemoting;

  public OnTopicSubscribedEmptyProcessor(AcmedcareMQRemoting acmedcareMQRemoting) {
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

      byte[] content = remotingCommand.getBody();
      if (content != null && content.length > 0) {

        final OnTopicSubscribeEmptyEventData data =
            JSON.parseObject(content, OnTopicSubscribeEmptyEventData.class);

        if (data == null) {
          throw new BizException("received broadcast event payload must not be null.");
        }

        acmedcareMQRemoting
            .eventBus()
            .post(
                new AcmedcareEvent() {
                  @Override
                  public Event eventType() {
                    return BizEvent.ON_TOPIC_EMPTY_SUBSCRIBED_EVENT;
                  }

                  @Override
                  public Object data() {
                    return data;
                  }
                });

        response.setBody(BizResult.SUCCESS.bytes());
      }
    } catch (Exception e) {
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
