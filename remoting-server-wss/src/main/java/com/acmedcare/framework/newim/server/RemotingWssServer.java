package com.acmedcare.framework.newim.server;

import com.acmedcare.framework.boot.snowflake.EnableSnowflake;
import com.acmedcare.framework.boot.snowflake.Snowflake;
import com.acmedcare.framework.boot.web.socket.standard.ServerEndpointExporter;
import com.acmedcare.framework.newim.deliver.connector.EnableDelivererConnector;
import com.acmedcare.framework.newim.deliver.connector.client.DelivererClientInitializer;
import com.acmedcare.framework.newim.server.core.NewIMServerBootstrap;
import com.acmedcare.framework.newim.server.core.connector.ClusterReplicaConnector;
import com.acmedcare.framework.newim.server.core.connector.MasterConnector;
import com.acmedcare.framework.newim.server.listener.DelivererConnectorWssServerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static com.acmedcare.framework.newim.deliver.connector.client.DelivererClientMarkerConfiguration.DELIVERER_CLIENT_INITIALIZER_BEAN_NAME;
import static com.acmedcare.framework.newim.server.ClusterLogger.startLog;

/**
 * Remoting Wss Server Application
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 08/11/2018.
 */
@SpringBootApplication
@ComponentScan("com.acmedcare.framework.newim")
@EnableDelivererConnector(enabledClient = true, listeners = {DelivererConnectorWssServerListener.class})
public class RemotingWssServer {

  /** Spring Context Instance of {@link org.springframework.context.ApplicationContext} */
  private static ConfigurableApplicationContext context;

  public static void main(String[] args) {
    context =
        new SpringApplicationBuilder()
            .sources(RemotingWssServer.class)
            .web(WebApplicationType.SERVLET)
            .run(args);

    startLog.info("[WSS] startup NewIM Server");
    NewIMServerBootstrap bootstrap = context.getBean(NewIMServerBootstrap.class);
    bootstrap.startup(0);

    startLog.info("[WSS] startup Master(s) Connector.");
    MasterConnector masterConnector = context.getBean(MasterConnector.class);
    masterConnector.start();

    startLog.info("[WSS] startup NewIM Cluster(s) Connector.");
    ClusterReplicaConnector clusterReplicaConnector =
        context.getBean(ClusterReplicaConnector.class);
    clusterReplicaConnector.start();

    // @since 2.3.0 add deliverer connector support
    startLog.info("[WSS] startup NewIM Deliverer Connector.");
    if (context.containsBean(DELIVERER_CLIENT_INITIALIZER_BEAN_NAME)) {

      DelivererClientInitializer delivererClientInitializer =
          context.getBean(DELIVERER_CLIENT_INITIALIZER_BEAN_NAME,DelivererClientInitializer.class);

      delivererClientInitializer.startup();
    }

    startLog.info("[WSS] register jvm shutdown hook .");
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  long start = System.currentTimeMillis();
                  // shutdown main server
                  startLog.info("[WSS] jvm shutdown hook, shutdown new-im main server.");
                  try {
                    bootstrap.shutdown(false);
                  } catch (Exception ignore) {
                  }

                  // shutdown master connector
                  startLog.info("[WSS] jvm shutdown hook, shutdown master connector.");
                  try {
                    masterConnector.shutdown();
                  } catch (Exception ignore) {
                  }

                  // shutdown cluster defaultReplica connector
                  startLog.info("[WSS] jvm shutdown hook, shutdown cluster defaultReplica connector.");
                  try {
                    clusterReplicaConnector.shutdown();
                  } catch (Exception ignore) {
                  }

                  startLog.info(
                      "[WSS] jvm shutdown completed , time:{} ms",
                      (System.currentTimeMillis() - start));
                }));
  }

  @Configuration
  public static class WebSocketConfiguration {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
      return new ServerEndpointExporter();
    }
  }

  @Configuration
  @EnableSnowflake(workerId = "${snowflake.worker-id:1}")
  public static class Ids {
    public static Snowflake snowflake;

    @Autowired
    public void setSnowflake(Snowflake snowflake) {
      Ids.snowflake = snowflake;
    }
  }
}
