package com.acmedcare.framework.newim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Master Logger(s) Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 14/11/2018.
 */
public class MasterLogger {

  /** Master Replica Logger */
  public static final Logger masterReplicaLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.masterReplicaLog");

  /** Replica Logger Client */
  public static final Logger masterClusterAcceptorLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.masterClusterAcceptorLog");

  public static final Logger masterReplicaClientLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.masterReplicaClientLog");

  public static final Logger startLog =
      LoggerFactory.getLogger("com.acmedcare.framework.newim.startLog");
}
