package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.biz.bean.Topic;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * NewTopicsRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class NewTopicsRequest extends BaseRequest {

  private List<NewTopicRequest> newTopicRequests;

  @Override
  public void validateFields() throws BizException {
    if (newTopicRequests != null && !newTopicRequests.isEmpty()) {
      for (NewTopicRequest newTopicRequest : newTopicRequests) {
        newTopicRequest.validateFields();
      }
    } else {
      throw new BizException("request must not be null.");
    }
  }

  public interface Callback {

    /**
     * Succeed callback
     *
     * @param topics topics
     */
    void onSuccess(List<Topic> topics);

    /**
     * Sub failed callback
     *
     * @param code error code
     * @param message error message
     */
    void onFailed(int code, String message);

    /**
     * On Exception
     *
     * @param e exception
     */
    void onException(BizException e);
  }
}
