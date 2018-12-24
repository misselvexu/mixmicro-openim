package com.acmedcare.framework.remoting.mq.client.biz.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SendTopicMessageRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class SendTopicMessageRequest extends BaseRequest {

  /** Message Topic Id */
  private Long topicId;

  /** Topic Tag */
  private String topicTag;

  /** Topic Message Content */
  private byte[] content;

  public interface Callback {

    /**
     * Sub Succeed callback
     *
     * @param messageId return topic message Id
     */
    void onSuccess(Long messageId);

    /**
     * Sub failed callback
     *
     * @param code error code
     * @param message error message
     */
    void onFailed(int code, String message);
  }
}
