package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.Constants;
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

  @CFNotNull private String namespace = Constants.DEFAULT_NAMESPACE;

  @CFNotNull private String passportId;

  /**
   * 群组编号
   *
   * <p>
   */
  @CFNotNull private String sender;

  /**
   * 消息Id , 可理解为最新的消息,那么之前的消息都标识阅读
   *
   * <p>
   */
  @CFNotNull private String messageId;

  /** 消息类型 */
  @CFNotNull private String messageType;

  @Builder
  public PushMessageStatusHeader(
      String passportId, String sender, String messageId, String messageType,String namespace) {
    this.passportId = passportId;
    this.sender = sender;
    this.messageId = messageId;
    this.messageType = messageType;
    this.namespace = namespace;
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
