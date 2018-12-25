package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * NewTopicHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
public class NewTopicHeader implements CommandCustomHeader {

  @CFNotNull private String namespace = "MQ-DEFAULT";

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

  /** 主题标识 */
  @CFNotNull private String topicTag;
  /** 主题名称 */
  @CFNotNull private String topicName;

  /** 主题描述 */
  private String topicDesc;

  /** 主题扩展信息 */
  private String topicExt;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
