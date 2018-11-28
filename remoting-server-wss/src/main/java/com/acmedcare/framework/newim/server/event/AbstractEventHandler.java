package com.acmedcare.framework.newim.server.event;

import com.google.common.eventbus.Subscribe;

/**
 * Abstract enent handler
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 28/11/2018.
 */
public abstract class AbstractEventHandler<T> {

  /**
   * Add event execute handler
   *
   * @param event acmedcare event wrapper
   */
  @Subscribe
  public abstract void execute(Event<T> event);
}
