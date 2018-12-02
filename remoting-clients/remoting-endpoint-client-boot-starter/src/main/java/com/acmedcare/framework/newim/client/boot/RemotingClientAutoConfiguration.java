package com.acmedcare.framework.newim.client.boot;

import com.acmedcare.framework.newim.client.boot.RemotingClientAutoConfiguration.RemotingMasterEndpointClientServiceProperties;
import com.acmedcare.framework.newim.client.boot.RemotingClientAutoConfiguration.RemotingNasClientServiceProperties;
import com.acmedcare.framework.newim.client.exception.EndpointException;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointClient;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointFactory;
import com.acmedcare.framework.newim.master.endpoint.client.MasterEndpointProperties;
import com.acmedcare.nas.client.NasProperties;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnClass(MasterEndpointClient.class)
@EnableConfigurationProperties({
  RemotingMasterEndpointClientServiceProperties.class,
  RemotingNasClientServiceProperties.class
})
public class RemotingClientAutoConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(RemotingClientAutoConfiguration.class);

  @Bean
  @ConditionalOnProperty(name = "remoting.nas.endpoint.enabled", havingValue = "false")
  public MasterEndpointClient masterEndpointClient(
      RemotingMasterEndpointClientServiceProperties properties) {

    if (properties.getRemoteAddr() == null || properties.getRemoteAddr().trim().length() == 0) {
      throw new EndpointException(
          "Master endpoint init failed, remoteAddr must not be null; config properties 'emoting.master.endpoint.remote-addr' in you application.properties file;");
    }

    MasterEndpointProperties masterEndpointProperties =
        new MasterEndpointProperties(
            Lists.newArrayList(properties.getRemoteAddr().split(",")), properties.isHttps());

    LOG.info(
        "Remoting endpoint client is already inited ,Properties is: {}",
        JSON.toJSONString(masterEndpointProperties));

    return MasterEndpointFactory.newMasterEndpointClient(masterEndpointProperties);
  }

  @Bean
  @ConditionalOnProperty(name = "remoting.nas.endpoint.enabled", havingValue = "true")
  public MasterEndpointClient masterEndpointClientWithNasSupport(
      RemotingMasterEndpointClientServiceProperties properties,
      RemotingNasClientServiceProperties remotingNasClientServiceProperties) {

    if (properties.getRemoteAddr() == null || properties.getRemoteAddr().trim().length() == 0) {
      throw new EndpointException(
          "Master endpoint init failed, remoteAddr must not be null; config properties 'emoting.master.endpoint.remote-addr' in you application.properties file;");
    }

    MasterEndpointProperties masterEndpointProperties =
        new MasterEndpointProperties(
            Lists.newArrayList(properties.getRemoteAddr().split(",")), properties.isHttps());

    LOG.info(
        "Remoting endpoint client is already inited ,Properties is: {}",
        JSON.toJSONString(masterEndpointProperties));

    if (remotingNasClientServiceProperties.getRemoteAddr() == null
        || remotingNasClientServiceProperties.getRemoteAddr().trim().length() == 0) {
      throw new EndpointException(
          "Master endpoint init failed, nas remoteAddr must not be null; config properties 'emoting.nas.endpoint.remote-addr' in you application.properties file;");
    }

    NasProperties nasProperties = new NasProperties();
    nasProperties.setServerAddrs(
        Lists.newArrayList(remotingNasClientServiceProperties.getRemoteAddr().split(",")));
    nasProperties.setAppId(remotingNasClientServiceProperties.getNasAppId());
    nasProperties.setAppKey(remotingNasClientServiceProperties.getNasAppKey());
    nasProperties.setHttps(remotingNasClientServiceProperties.isHttps());

    LOG.info(
        "Remoting nas endpoint client is already inited ,Properties is: {}",
        JSON.toJSONString(nasProperties));

    return MasterEndpointFactory.newMasterEndpointClient(masterEndpointProperties, nasProperties);
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

  @ConditionalOnProperty(name = "remoting.nas.endpoint.enabled", havingValue = "true")
  @ConfigurationProperties(prefix = "remoting.nas.endpoint")
  public static class RemotingNasClientServiceProperties {

    private boolean enabled;

    private String remoteAddr;

    private boolean https = false;

    private String nasAppId = "DEFAULT#NAS#APPID";

    private String nasAppKey = "DEFAULT#NAS#APPKEY";

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

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

    public String getNasAppId() {
      return nasAppId;
    }

    public void setNasAppId(String nasAppId) {
      this.nasAppId = nasAppId;
    }

    public String getNasAppKey() {
      return nasAppKey;
    }

    public void setNasAppKey(String nasAppKey) {
      this.nasAppKey = nasAppKey;
    }
  }
}
