package com.acmedcare.framework.remoting.mq.client.biz.request;

import com.acmedcare.tiffany.framework.remoting.android.core.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.android.core.exception.RemotingCommandException;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.CommandCustomHeader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SendTopicMessageHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
@Getter
@Setter
@NoArgsConstructor
public class SendTopicMessageHeader implements CommandCustomHeader {

  @CFNotNull private String namespace = "MQ-DEFAULT";

  private String passport;

  private String passportId;

  /** Message Topic Id */
  private Long topicId;

  /** Topic Tag */
  private String topicTag;

  /** Topic Message Content */
  // private byte[] content;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
