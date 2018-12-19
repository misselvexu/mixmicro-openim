package com.acmedcare.framework.newim.server.mq.processor.header;

import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.tiffany.framework.remoting.CommandCustomHeader;
import com.acmedcare.tiffany.framework.remoting.annotation.CFNotNull;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingCommandException;
import lombok.Getter;
import lombok.Setter;

/**
 * MonitorClientTopicSubscribeHeader
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
@Getter
@Setter
public class MonitorClientTopicSubscribeHeader implements CommandCustomHeader {

  /** Namespace */
  private String namespace = InstanceType.MQ_SERVER.name();
  // account fields
  @CFNotNull private String passport;
  @CFNotNull private Long passportId;


  @Override
  public void checkFields() throws RemotingCommandException {}
}
