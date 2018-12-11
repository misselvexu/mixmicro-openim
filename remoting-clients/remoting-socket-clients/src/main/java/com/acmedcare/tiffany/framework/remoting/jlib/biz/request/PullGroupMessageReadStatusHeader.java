package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Pull Group Message Read Status Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
@Getter
@Setter
public class PullGroupMessageReadStatusHeader implements CommandCustomHeader {

  @CFNotNull private String namespace = Constants.DEFAULT_NAMESPACE;

  /**
   * 群组编号
   *
   * <p>
   */
  @CFNotNull private String groupId;

  /**
   * 消息Id , 可理解为最新的消息,那么之前的消息都标识阅读
   *
   * <p>
   */
  @CFNotNull private String messageId;

  @Builder
  public PullGroupMessageReadStatusHeader(String groupId, String messageId,String namespace) {
    this.groupId = groupId;
    this.messageId = messageId;
    this.namespace = namespace;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
