package com.acmedcare.framework.newim.server.processor;

import com.acmedcare.tiffany.framework.remoting.netty.NettyRequestProcessor;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract Normal Request Processor
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 13/11/2018.
 */
@Getter
@Setter
public abstract class AbstractClusterRequestProcessor implements NettyRequestProcessor {

  @Override
  public boolean rejectRequest() {
    return false;
  }
}
