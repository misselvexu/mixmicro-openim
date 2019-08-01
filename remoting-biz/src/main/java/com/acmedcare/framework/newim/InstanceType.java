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

  /** MQ Server */
  MQ_SERVER,

  /**
   * Deliverer Server
   *
   * @since 2.3.0
   */
  DELIVERER
}
