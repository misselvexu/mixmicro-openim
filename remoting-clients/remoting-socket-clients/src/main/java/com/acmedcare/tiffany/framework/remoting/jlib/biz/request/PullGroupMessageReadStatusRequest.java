package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Member;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * com.acmedcare.tiffany.framework.remoting.jlib.biz.request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullGroupMessageReadStatusRequest extends BaseRequest {

  private String namespace = Constants.DEFAULT_NAMESPACE;

  /**
   * 群组编号
   *
   * <p>
   */
  private String groupId;

  /**
   * 消息Id , 可理解为最新的消息,那么之前的消息都标识阅读
   *
   * <p>
   */
  private String messageId;

  public interface Callback {

    void onSuccess(List<Member> readedMembers, List<Member> unReadMembers);

    void onFailed(int code, String message);
  }
}
