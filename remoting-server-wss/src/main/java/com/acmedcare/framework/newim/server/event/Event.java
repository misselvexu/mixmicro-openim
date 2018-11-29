package com.acmedcare.framework.newim.server.event;

import java.util.List;

/**
 * Event Defined
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 12/11/2018.
 */
public interface Event<T> {

  public abstract T data();

  /**
   * 获取到新节点回调事件
   *
   * <p>
   */
  public static class FetchNewClusterReplicaServerEvent implements Event<List<String>> {

    private List<String> addresses;

    public FetchNewClusterReplicaServerEvent(List<String> addresses) {
      this.addresses = addresses;
    }

    @Override
    public List<String> data() {
      return addresses;
    }
  }
}
