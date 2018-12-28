package com.acmedcare.framework.newim.server;

/**
 * Context
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public interface Context {

  /**
   * Return Current Context
   *
   * @return context
   */
  default Context context() {
    return this;
  }
}
