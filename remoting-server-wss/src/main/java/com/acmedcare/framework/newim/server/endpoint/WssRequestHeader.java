package com.acmedcare.framework.newim.server.endpoint;

import com.acmedcare.framework.newim.server.exception.InvalidRequestHeaderException;

/**
 * Wss Request Header
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 19/11/2018.
 */
public interface WssRequestHeader {

  /**
   * Check Headers Method
   *
   * @throws InvalidRequestHeaderException invalid request header exception
   */
  void checkHeaders() throws InvalidRequestHeaderException;
}
