package com.acmedcare.framework.newim.server.mq;

import com.acmedcare.framework.newim.server.Server;
import com.acmedcare.framework.newim.spi.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQ Server
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@Extension("mqserver")
public class MQServer implements Server {

  private static final Logger logger = LoggerFactory.getLogger(MQServer.class);

  /**
   * Server Startup Method
   *
   * @param properties server config properties
   * @return server instance
   */
  @Override
  public Server startup(ServerProperties properties) {
    logger.info("[MQServer] ready to startup mq-server...");

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
