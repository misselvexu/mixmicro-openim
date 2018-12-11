package com.acmedcare.framework.newim.server.processor.header;

import com.acmedcare.framework.newim.Message;
import com.acmedcare.framework.newim.client.MessageConstants;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Push Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public class PushMessageHeader implements CommandCustomHeader {
  private String namespace = MessageConstants.DEFAULT_NAMESPACE;
  @CFNotNull private String passport;
  @CFNotNull private String passportId;
  @CFNotNull private String messageType;

  /** 是否开启QOS,默认值为 false */
  private boolean qos = false;

  /** 最大重试次数 */
  private int maxRetryTimes = 1;

  /** 重试间隔 */
  private long retryPeriod = 5000;

  /**
   * Decode Message Type
   *
   * @return type enum
   */
  public Message.MessageType decodeType() {
    return Message.MessageType.valueOf(messageType);
  }

  @Override
  public void checkFields() throws RemotingCommandException {
    if (StringUtils.isAnyBlank(passportId, passport, messageType)) {
      throw new RemotingCommandException("通行证,编号,消息类型不能为空");
    }
  }
}
