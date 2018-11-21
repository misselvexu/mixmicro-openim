package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Message Request
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullMessageRequest {

  private String sender;

  /**
   * InnerType 0默认单聊 ,1-群组
   *
   * <pre>
   *
   * </pre>
   */
  private int type;

  /**
   * 最新的消息 ID
   *
   * <pre></pre>
   */
  private long leastMessageId;

  private long limit;

  private String username;

  @Builder
  public PullMessageRequest(
      String sender, int type, long leastMessageId, long limit, String username) {
    this.sender = sender;
    this.type = type;
    this.leastMessageId = leastMessageId;
    this.limit = limit;
    this.username = username;
  }

  public interface Callback {

    void onSuccess(List<Message> messages);

    void onFailed(int code, String message);
  }
}
