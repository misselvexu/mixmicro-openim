package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullMessageHeader extends BaseHeader implements CommandCustomHeader {

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

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

  @Builder
  public PullMessageHeader(
      String passport,
      String sender,
      int type,
      long leastMessageId,
      long limit,
      String passportId) {
    this.passport = passport;
    this.sender = sender;
    this.type = type;
    this.leastMessageId = leastMessageId;
    this.limit = limit;
    this.passportId = passportId;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
