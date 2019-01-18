package com.acmedcare.framework.newim;

/**
 * InstanceType
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-18.
 */
public enum InstanceType {

  /** Master Server */
  MASTER,

  /** Default IM Server */
  DEFAULT,

  /** @deprecated use {@link #DEFAULT} instead of */
  CLUSTER,

  /** Replica */
  REPLICA,

  /** Web Socket Server */
  WSS,

  /** Client */
  CLIENT,

  /** MQ Server */
  MQ_SERVER
}
