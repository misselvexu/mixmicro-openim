package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Pull Session List Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PullSessionStatusHeader extends BaseHeader implements CommandCustomHeader {

  @CFNotNull private String username;

  /**
   * InnerType 0默认单聊 session ,1-群组 session
   *
   * <pre>
   *
   * </pre>
   */
  @CFNotNull private int type;

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
  @CFNotNull private String flagId;

  @Builder
  public PullSessionStatusHeader(String username, int type, String flagId) {
    this.username = username;
    this.type = type;
    this.flagId = flagId;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
