package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.framework.remoting.mq.client.Serializables;
import com.acmedcare.framework.remoting.mq.client.exception.BizException;
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

  private String topicType;

  /** Topic Message Content */
  private byte[] content;

  @Override
  public void validateFields() throws BizException {
    if (Serializables.isAnyBlank(topicTag, topicType, getPassport(), getPassportId())) {
      throw new BizException("发送主题消息参数:[topicTag,topicType,passport,passportId]不能为空");
    }

    if (topicId == null || topicId < 0) {
      throw new BizException("主题标识ID不能为空");
    }

    if (content == null || content.length == 0) {
      throw new BizException("主题消息内容不能为空");
    }
  }

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

    void onException(BizException e);
  }
}
