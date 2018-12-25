package com.acmedcare.framework.remoting.mq.client.processor;

import com.acmedcare.framework.remoting.mq.client.AcmedcareLogger;
import com.acmedcare.framework.remoting.mq.client.AcmedcareMQRemoting;
import com.acmedcare.framework.remoting.mq.client.TopicMessageListener.ConsumeResult;
import com.acmedcare.framework.remoting.mq.client.biz.BizResult;
import com.acmedcare.framework.remoting.mq.client.biz.bean.Message;
import com.acmedcare.framework.remoting.mq.client.events.AcmedcareEvent;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import com.acmedcare.tiffany.framework.remoting.android.HandlerMessageListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRequestProcessor;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

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

      byte[] content = remotingCommand.getBody();
      if (content != null && content.length > 0) {
        AcmedcareLogger.i(null, "接受到主题消息:" + new String(content, "UTF-8"));
        final Message message = JSON.parseObject(content, Message.class);
        if (message != null) {
          // post event
          acmedcareMQRemoting
              .eventBus()
              .post(
                  new AcmedcareEvent() {
                    @Override
                    public Event eventType() {
                      return BizEvent.ON_TOPIC_MESSAGE_EVENT;
                    }

                    @Override
                    public Object data() {
                      return message;
                    }
                  });

          // callback
          ConsumeResult consumeResult =
              acmedcareMQRemoting.getTopicMessageListener().onMessages(Lists.newArrayList(message));

          // TODO return back consume result to mq server.

          response.setBody(BizResult.SUCCESS.bytes());

        } else {
          throw new BizException("decode mq topic message failed.");
        }
      } else {
        throw new BizException("received none content message from server.");
      }
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
