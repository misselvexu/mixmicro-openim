package com.acmedcare.microservices.im;

import com.acmedcare.microservices.im.core.ServerConfig;
import com.acmedcare.microservices.im.core.TiffanySocketServer;
import com.acmedcare.microservices.im.storage.IPersistenceExecutor;
import com.acmedcare.tiffany.framework.remoting.netty.NettyServerConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import xyz.vopen.microservices.surface.AbstractTiffanyApplication;
import xyz.vopen.microservices.surface.ApplicationRunner;
import xyz.vopen.tiffany.swagger.EnableSwagger2;

/**
 * Remoting Application Main Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version v1.0 - 09/08/2018.
 */
@SpringBootApplication(scanBasePackageClasses = RemotingApplication.class)
@EnableTransactionManagement
@EnableSwagger2
public class RemotingApplication extends AbstractTiffanyApplication {

  private TiffanySocketServer tiffanySocketServer;

  public static void main(String[] args) {
    ApplicationRunner.test(RemotingApplication.class);
  }

  /**
   * 程序运行
   *
   * @throws Exception exception
   */
  @Override
  public void start(String[] args) throws Exception {
    SpringApplication.run(RemotingApplication.class, args);
    System.out.println("Start Application....");

    // start server
    NettyServerConfig nettyServerConfig = new NettyServerConfig();
    nettyServerConfig.setListenPort(Configurations.serverConfig.getPort());
    nettyServerConfig.setServerChannelMaxIdleTimeSeconds(20);

    tiffanySocketServer = new TiffanySocketServer(nettyServerConfig);
    tiffanySocketServer.start();
  }

  /**
   * 程序停止
   *
   * @throws Exception exception
   */
  @Override
  public void shutdown() throws Exception {
    System.out.println("Shutdown Application ....");
    if (tiffanySocketServer != null) {
      this.tiffanySocketServer.destory();
    }
  }

  @Bean
  @Primary
  public PlatformTransactionManager transactionManager(HikariDataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }

  @Bean
  @Primary
  public TransactionTemplate transactionTemplate(
      PlatformTransactionManager platformTransactionManager) {
    return new TransactionTemplate(platformTransactionManager);
  }

  @Bean
  @Primary
  public IdHelper newIdHelper() {
    return new IdHelper();
  }

  @Configuration
  public static class Ids {
    public static IdHelper idHelper;

    @Autowired
    public void setIdHelper(IdHelper idHelper) {
      Ids.idHelper = idHelper;
    }
  }

  @Configuration
  public static class Datas {

    /** Persistent Instance */
    public static IPersistenceExecutor persistenceExecutor;

    @Autowired
    public void setPersistenceExecutor(IPersistenceExecutor persistenceExecutor) {
      Datas.persistenceExecutor = persistenceExecutor;
    }
  }

  @Configuration
  public static class Configurations {
    static ServerConfig serverConfig;

    @Autowired
    public void setServerConfig(ServerConfig serverConfig) {
      Configurations.serverConfig = serverConfig;
    }
  }
}
