package com.acmedcare.framework.newim.client.boot;

import com.acmedcare.framework.newim.client.boot.RemotingClientAutoConfiguration.RemotingMasterEndpointClientServiceProperties;
import com.acmedcare.framework.newim.client.exception.EndpointException;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointFactory;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointProperties;
import com.google.common.collect.Lists;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Remoting Client Auto Configuration
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 12/10/2018.
 */
@Configuration
@EnableConfigurationProperties(RemotingMasterEndpointClientServiceProperties.class)
public class RemotingClientAutoConfiguration {

  @Bean
  public MasterEndpointClient masterEndpointClient(
      RemotingMasterEndpointClientServiceProperties properties) {

    if (properties.getRemoteAddr() == null || properties.getRemoteAddr().trim().length() == 0) {
      throw new EndpointException(
          "Master endpoint init failed, remoteAddr must not be null; config properties 'emoting.master.endpoint.remote-addr' in you application.properties file;");
    }

    MasterEndpointProperties masterEndpointProperties =
        new MasterEndpointProperties(
            Lists.newArrayList(properties.getRemoteAddr().split(",")), properties.isHttps());

    return MasterEndpointFactory.newMasterEndpointClient(masterEndpointProperties);
  }

  @ConfigurationProperties(prefix = "remoting.master.endpoint")
  public static class RemotingMasterEndpointClientServiceProperties {

    private String remoteAddr;

    private boolean https = false;

    public String getRemoteAddr() {
      return remoteAddr;
    }

    public void setRemoteAddr(String remoteAddr) {
      this.remoteAddr = remoteAddr;
    }

    public boolean isHttps() {
      return https;
    }

    public void setHttps(boolean https) {
      this.https = https;
    }
  }
}
