package com.acmedcare.framework.newim.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master Logger(s) Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
public class ClusterLogger {

  /** Master Replica Logger */
  public static final Logger masterClusterLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.masterClusterLog");

  public static final Logger masterReplicaClientLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.clientLog");

  public static final Logger startLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.startLog");
}
