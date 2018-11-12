package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Message Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullMessageHeader extends BaseHeader implements CommandCustomHeader {

  /** username for client */
  @CFNotNull private String username;

  @CFNotNull private String sender;

  /**
   * InnerType 0默认单聊 ,1-群组
   *
   * <pre>
   *
   * </pre>
   */
  @CFNotNull private int type;

  /**
   * 最新的消息 ID
   *
   * <pre></pre>
   */
  @CFNotNull private long leastMessageId;

  @CFNotNull private long limit;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
