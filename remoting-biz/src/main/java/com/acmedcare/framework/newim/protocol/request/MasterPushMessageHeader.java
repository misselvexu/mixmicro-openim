package com.acmedcare.framework.newim.protocol.request;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * Master Push Message Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 21/11/2018.
 */
@Getter
@Setter
public class MasterPushMessageHeader implements CommandCustomHeader {

  /** 质量保证 */
  @CFNotNull private boolean qos;

  /** 发送失败后,最大重试次数 */
  @CFNotNull private int maxRetryTimes;

  /** 消息重试间隔 */
  @CFNotNull private long retryPeriod;

  /** 是否持久化消息 */
  @CFNotNull private boolean persistent;

  /** 消息类型(单聊/群聊) */
  @CFNotNull private String messageType;

  /** 普通消息/指令消息 */
  @CFNotNull private String innerType;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
