package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Push Message Status
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
public class PushMessageStatusHeader extends BaseHeader implements CommandCustomHeader {

  /** passport for client */
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

  /**
   * 根据PMT的类型处理对应的消息已读
   *
   * @return PMT
   */
  public PMT decodePMTValue() {
    if (StringUtils.isNoneBlank(pmt)) {
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
