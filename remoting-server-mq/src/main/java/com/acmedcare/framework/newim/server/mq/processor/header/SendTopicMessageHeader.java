package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
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

  @CFNotNull private String passport;

  @CFNotNull private String passportId;

  /** Message Topic Id */
  @CFNotNull private Long topicId;

  /** Topic Tag */
  @CFNotNull private String topicTag;

  /** Topic Message Content */
  private byte[] content;

  @Override
  public void checkFields() throws RemotingCommandException {}
}
