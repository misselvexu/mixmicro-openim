package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Session;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Session List
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullSessionListRequest extends BaseRequest {

  @Builder
  public PullSessionListRequest(String username) {
    super(username);
  }

  public interface Callback {

    void onSuccess(List<Session> list);

    void onFailed(int code, String message);
  }
}
