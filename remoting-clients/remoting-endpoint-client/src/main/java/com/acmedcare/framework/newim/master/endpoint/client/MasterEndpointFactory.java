package com.acmedcare.framework.newim.master.endpoint.client;

import com.acmedcare.framework.newim.client.exception.EndpointException;
import com.acmedcare.nas.client.NasClient;
import com.acmedcare.nas.client.NasClientFactory;
import com.acmedcare.nas.client.NasProperties;
import java.lang.reflect.Constructor;
import java.util.List;

/**
 * Master Endpoint Client Factory
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 29/11/2018.
 */
public class MasterEndpointFactory {

  /**
   * Create <code>{@link MasterEndpointClient}</code> instance
   *
   * @param properties endpoint config properties of {@link MasterEndpointProperties}
   * @return a instance of {@link MasterEndpointClient}
   * @throws EndpointException throw exception of {@link EndpointException}
   */
  public static MasterEndpointClient newMasterEndpointClient(MasterEndpointProperties properties)
      throws EndpointException {

    try {

      if (properties.getRemotingAddresses() == null
          || properties.getRemotingAddresses().isEmpty()) {
        throw new RuntimeException("master address list must not be empty.");
      }

      Class<?> driverImplClass =
          Class.forName(
              "com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient");
      Constructor constructor = driverImplClass.getConstructor(List.class, boolean.class);

      return (MasterEndpointClient)
          constructor.newInstance(properties.getRemotingAddresses(), properties.isHttps());

    } catch (Exception e) {
      throw new EndpointException("init master endpoint client failed", e);
    }
  }

  /**
   * Create <code>{@link MasterEndpointClient}</code> instance
   *
   * @param properties endpoint config properties of {@link MasterEndpointProperties}
   * @return a instance of {@link MasterEndpointClient}
   * @throws EndpointException throw exception of {@link EndpointException}
   */
  public static MasterEndpointClient newMasterEndpointClient(
      MasterEndpointProperties properties, NasProperties nasProperties) throws EndpointException {

    try {

      if (properties.getRemotingAddresses() == null
          || properties.getRemotingAddresses().isEmpty()) {
        throw new RuntimeException("master address list must not be empty.");
      }

      if (nasProperties.getServerAddrs() == null || nasProperties.getServerAddrs().isEmpty()) {
        throw new RuntimeException("nas address list must not be empty.");
      }

      Class<?> driverImplClass =
          Class.forName(
              "com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient");
      Constructor constructor =
          driverImplClass.getConstructor(List.class, boolean.class, NasClient.class);

      NasClient nasClient = NasClientFactory.createNewNasClient(nasProperties);

      return (MasterEndpointClient)
          constructor.newInstance(
              properties.getRemotingAddresses(), properties.isHttps(), nasClient);

    } catch (Exception e) {
      throw new EndpointException("init master endpoint client failed", e);
    }
  }
}
