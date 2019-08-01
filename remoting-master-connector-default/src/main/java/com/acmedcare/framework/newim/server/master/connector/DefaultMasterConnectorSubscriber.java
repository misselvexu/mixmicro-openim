package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.event.Event;
import com.acmedcare.framework.newim.server.master.connector.event.PullClusterEvent;

/**
 * MasterConnectorSubscriber
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public class DefaultMasterConnectorSubscriber extends MasterConnectorSubscriber {

  private final DefaultMasterConnectorContext context;

  public DefaultMasterConnectorSubscriber(DefaultMasterConnectorContext context, boolean sync) {
    super(context,sync);
    this.context = context;
  }

  @Override
  public void onEvent(Event event) {

    Class aClass = event.getClass();

    if (aClass == PullClusterEvent.class) {
      PullClusterEvent pullClusterEvent = (PullClusterEvent) event;
      this.context.onPullClusterReplicas(pullClusterEvent.getClusterReplicas());
    }
  }
}
