package com.acmedcare.framework.newim.server.replica;

import java.io.Serializable;

/**
 * NodeInstance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-25.
 */
public class NodeInstance implements Serializable {

  /** Replica Host */
  private String host;

  /** Replica port */
  private int port;
}
