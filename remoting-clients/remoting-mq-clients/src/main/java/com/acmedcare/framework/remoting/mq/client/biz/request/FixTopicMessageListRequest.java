package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.biz.bean.Message;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PullTopicListRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class FixTopicMessageListRequest extends BaseRequest {

  private Long topicId;

  /** last message id */
  private Long lastTopicMessageId;

  private int limit = 10;

  @Override
  public void validateFields() throws BizException {

    if (Serializables.isAnyBlank(getPassport(), getPassportId())) {
      throw new BizException("补漏消息请求参数:[passportId,passport]不能为空");
    }

    if (topicId <= 0 || lastTopicMessageId <= 0) {
      throw new BizException("补漏消息请求参数:[topicId,lastTopicMessageId]不能为空");
    }
  }

  public interface Callback {

    /**
     * Succeed callback
     *
     * @param messages message list
     */
    void onSuccess(List<Message> messages);

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
