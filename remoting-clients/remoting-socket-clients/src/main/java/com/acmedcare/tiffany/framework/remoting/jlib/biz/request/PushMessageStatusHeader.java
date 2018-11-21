package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Push Message Status
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PushMessageStatusHeader extends BaseHeader implements CommandCustomHeader {

  @CFNotNull private String username;

  /**
   * 最新消息 ID
   *
   * <pre>根据客户端群组最后一条消息的编号,反推,界限不是显示,而是全部</pre>
   */
  @CFNotNull private long leastMessageId;

  /** 此处 ID 标识单聊(发送人) / 群组的标识 */
  @CFNotNull private String sender;

  @CFNotNull private String pmt;

  @Builder
  public PushMessageStatusHeader(String username, long leastMessageId, String sender, String pmt) {
    this.username = username;
    this.leastMessageId = leastMessageId;
    this.sender = sender;
    this.pmt = pmt;
  }

  /**
   * 根据PMT的类型处理对应的消息已读
   *
   * @return PMT
   */
  public PMT decodePMTValue() {
    if (pmt != null && pmt.length() > 0) {
      return PMT.valueOf(pmt);
    }
    return null;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}

  public enum PMT {
    GROUP,
    SINGLE
  }
}
