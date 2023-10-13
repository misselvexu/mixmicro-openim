package com.acmedcare.framework.newim.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master Logger(s) Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
public final class ClusterLogger {

  /** Master Replica Logger */
  public static final Logger masterClusterLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.masterClusterLog");

  public static final Logger clusterReplicaLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.clusterReplicaLog");

  public static final Logger imServerLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.imServerLog");

  public static final Logger convertLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.convertLog");

  public static final Logger wssServerLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.wssServerLog");

  public static final Logger innerReplicaServerLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.innerReplicaServerLog");

  public static final Logger startLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.startLog");
}
