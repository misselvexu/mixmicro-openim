package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * RemoveTopicRequest
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class RemoveTopicRequest extends BaseRequest {

  /** 主题标识 */
  private Long topicId;

  @Override
  public void validateFields() throws BizException {
    if (Serializables.isAnyBlank(this.getPassport(), this.getPassportId())) {
      throw new BizException("New topic request params:[passport,passportId] can't be null.");
    }
  }

  public interface Callback {

    /** Succeed callback */
    void onSuccess();

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
