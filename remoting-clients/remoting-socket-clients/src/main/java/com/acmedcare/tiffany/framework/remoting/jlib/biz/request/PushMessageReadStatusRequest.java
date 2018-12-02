package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

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
public class PushMessageReadStatusRequest extends BaseRequest {

  /**
   * 最新消息 ID
   *
   * <pre>根据客户端群组最后一条消息的编号,反推,界限不是显示,而是全部</pre>
   */
  private long leastMessageId;

  /** 此处 ID 标识单聊(发送人) / 群组的标识 */
  private String sender;

  private String pmt;

  public interface Callback {
    void onSuccess();

    void onFailed(int code, String message);
  }
}
