package com.acmedcare.framework.newim.server.event;

/**
 * Event Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public interface Event {

  /**
   * 通讯节点事件
   *
   * <p>
   */
  enum ClusterEvent implements Event {}

  /**
   * Master事件
   *
   * <p>
   */
  enum MasterEvent implements Event {}
}
