package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.spi.Extension;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * MQ Server
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@Extension("mqserver")
public class MQServer implements Server {

  private static final Logger logger = LoggerFactory.getLogger(MQServer.class);

  @Autowired private MQServerProperties mqServerProperties;

  /**
   * Server Startup Method
   *
   * @return server instance
   */
  @Override
  public Server startup() {
    logger.info("[MQServer] ready to startup mq-server...");
    logger.info("Configuration: {}", JSON.toJSONString(mqServerProperties));
    return this;
  }

  /**
   * Shutdown Server
   *
   * <p>
   */
  @Override
  public void shutdown() {
    logger.info("[MQServer] ready to shutdown mq-server...");
  }
}
