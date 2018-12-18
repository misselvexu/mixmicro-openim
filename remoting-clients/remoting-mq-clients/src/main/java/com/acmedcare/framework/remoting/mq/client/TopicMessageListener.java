package com.acmedcare.framework.remoting.mq.client;

import com.acmedcare.framework.remoting.mq.client.biz.bean.Message;
import java.util.List;

/**
 * Topic Message Listener
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
public interface TopicMessageListener {

  /**
   * On Topic Message Received Callback
   *
   * @param messages message list
   * @return consume result
   */
  ConsumeResult onMessages(List<Message> messages);

  /** Consume Result Defined */
  public enum ConsumeResult {

    /** 消费成功 */
    CONSUMED,

    /** 消费失败 */
    FAILED
  }
}
