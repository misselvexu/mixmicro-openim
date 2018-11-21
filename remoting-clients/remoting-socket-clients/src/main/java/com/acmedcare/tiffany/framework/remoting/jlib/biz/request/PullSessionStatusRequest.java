package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Session;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Session Status
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullSessionStatusRequest extends BaseRequest {

  /**
   * InnerType 0默认单聊 session ,1-群组 session
   *
   * <pre>
   *
   * </pre>
   */
  private int type;

  /**
   * 标记 ID
   *
   * <pre>
   *
   *  <li>type=0 -receiver id
   *  <li>type=1 -group id
   *
   * </pre>
   */
  private String flagId;

  @Builder
  public PullSessionStatusRequest(String username, int type, String flagId) {
    super(username);
    this.type = type;
    this.flagId = flagId;
  }

  public interface Callback {

    void onSuccess(Session session);

    void onFailed(int code, String message);
  }
}
