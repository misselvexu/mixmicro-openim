package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.biz.bean.Topic;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * PullTopicListRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullTopicDetailRequest extends BaseRequest {

  private Long topicId;

  @Override
  public void validateFields() throws BizException {}

  public interface Callback {

    /**
     * Succeed callback
     *
     * @param topic topic
     */
    void onSuccess(Topic topic);

    /**
     * Sub failed callback
     *
     * @param code error code
     * @param message error message
     */
    void onFailed(int code, String message);

    void onException(BizException e);
  }
}
