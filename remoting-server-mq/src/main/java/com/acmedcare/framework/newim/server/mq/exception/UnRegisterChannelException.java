package com.acmedcare.framework.newim.server.mq.exception;

import com.acmedcare.tiffany.framework.remoting.exception.RemotingException;

/**
 * UnRegisterChannelException
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
public class UnRegisterChannelException extends RemotingException {

  public UnRegisterChannelException(String message) {
    super(message);
  }

  public UnRegisterChannelException(String message, Throwable cause) {
    super(message, cause);
  }
}
