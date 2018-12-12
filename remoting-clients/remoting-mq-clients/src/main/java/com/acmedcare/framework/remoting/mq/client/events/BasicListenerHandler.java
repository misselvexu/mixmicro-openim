package com.acmedcare.framework.remoting.mq.client.events;

import com.google.common.eventbus.Subscribe;

/**
 * Basic Event Listener Handler
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 30/07/2018.
 */
public abstract class BasicListenerHandler {

  /**
   * Add event execute handler
   *
   * @param acmedcareEvent acmedcare event wrapper
   */
  @Subscribe
  public abstract void execute(AcmedcareEvent acmedcareEvent);
}
