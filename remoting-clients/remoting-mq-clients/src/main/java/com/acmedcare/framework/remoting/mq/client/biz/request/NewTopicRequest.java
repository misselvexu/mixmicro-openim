package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NewTopicRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class NewTopicRequest extends BaseRequest {

  /** 主题名称 */
  private String topicName;

  /** 主题标识 */
  private String topicTag;

  @Override
  public void validateFields() throws BizException {
    if (Serializables.isAnyBlank(
        this.topicName, this.topicTag, this.getPassport(), this.getPassportId())) {
      throw new BizException(
          "New topic request params:[topicName,topicTag,passport,passportId] can't be null.");
    }
  }

  public interface Callback {

    /**
     * Succeed callback
     *
     * @param topicId topic id
     */
    void onSuccess(Long topicId);

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
