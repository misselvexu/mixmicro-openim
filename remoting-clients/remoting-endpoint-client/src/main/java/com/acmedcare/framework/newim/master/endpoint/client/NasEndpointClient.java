package com.acmedcare.framework.newim.master.endpoint.client;

import com.acmedcare.nas.client.NasClient;

/**
 * Nas Endpoint Client
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-02.
 */
public abstract class NasEndpointClient {

  protected final NasClient nasClient;

  public NasEndpointClient(NasClient nasClient) {
    this.nasClient = nasClient;
  }
}
