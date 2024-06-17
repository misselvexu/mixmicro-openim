package com.acmedcare.framework.newim.server;

/**
 * IdService
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-24.
 */
public interface IdService {

  /**
   * Get Next New Id
   *
   * @return next unique id
   * @throws Exception exception
   */
  long nextId() throws Exception;
}
