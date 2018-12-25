package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.biz.body.TopicSubscribeMapping.TopicMapping;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PullTopicSubscribedMappingsRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullTopicSubscribedMappingsRequest extends BaseRequest {

  private String topicId;

  @Override
  public void validateFields() throws BizException {
    if (Serializables.isAnyBlank(getPassport(), getPassportId(), topicId)) {
      throw new BizException("拉取主题订阅关系请求参数:[passport,passportId,topicId]不能为空");
    }
  }

  public interface Callback {

    /**
     * Succeed callback
     *
     * @param mappings Subscribe mappings
     */
    void onSuccess(List<TopicMapping> mappings);

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
