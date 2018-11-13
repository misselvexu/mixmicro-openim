package com.acmedcare.microservices.im.biz.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * Pull Session List Header
 *
 * @author Elve.Xu [iskp.me<at>gmail.com]
 * @version v1.0 - 09/08/2018.
 */
@Getter
@Setter
public class PullSessionStatusHeader extends BaseHeader implements CommandCustomHeader {

  /** passport for client */
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

  @Override
  public void checkFields() throws RemotingCommandException {}
}
