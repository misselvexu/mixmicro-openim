package com.acmedcare.tiffany.framework.remoting.jlib.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.jlib.biz.bean.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Push Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 10/08/2018.
 */
@Getter
@Setter
@NoArgsConstructor
public class PushMessageHeader extends BaseHeader implements CommandCustomHeader {

  private static final long serialVersionUID = -5953866493854736706L;
  @CFNotNull private String messageType;

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

  /** 质量保证 */
  @CFNotNull private boolean qos;

  /** 发送失败后,最大重试次数 */
  @CFNotNull private int maxRetryTimes;

  /** 消息重试间隔 */
  @CFNotNull private long retryPeriod;

  /** 是否持久化消息 */
  @CFNotNull private boolean persistent;

  @Builder
  public PushMessageHeader(
      String messageType,
      String passport,
      String passportId,
      boolean qos,
      int maxRetryTimes,
      long retryPeriod,
      boolean persistent) {
    this.messageType = messageType;
    this.passport = passport;
    this.passportId = passportId;
    this.qos = qos;
    this.maxRetryTimes = maxRetryTimes;
    this.retryPeriod = retryPeriod;
    this.persistent = persistent;
  }

  /**
   * Decode Message Type
   *
   * @return type enum
   */
  public Message.MessageType decodeType() {
    return Message.MessageType.valueOf(messageType);
  }

  @Override
  public void checkFields() throws RemotingCommandException {}
}
