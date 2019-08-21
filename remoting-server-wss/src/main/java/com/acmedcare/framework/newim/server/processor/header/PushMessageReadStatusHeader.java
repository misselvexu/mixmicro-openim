package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.framework.newim.Message.MessageType;
import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Push Message Read Status Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-05.
 * @since 2.2.0
 */
@Getter
@Setter
public class PushMessageReadStatusHeader implements CommandCustomHeader {

  private String namespace = MessageConstants.DEFAULT_NAMESPACE;

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

  public MessageType decodeMessageType() {
    return MessageType.valueOf(messageType.toUpperCase());
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isAnyBlank(passportId, sender, messageId, messageType)) {
      throw new RemotingCommandException(
          "推送消息读取状态请求参数[passportId,sender,messageId,messageType]不能为空");
    }
  }
}
