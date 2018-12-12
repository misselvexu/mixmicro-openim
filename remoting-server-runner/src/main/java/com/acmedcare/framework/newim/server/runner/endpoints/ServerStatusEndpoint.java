package com.acmedcare.framework.newim.server.runner.endpoints;

import com.acmedcare.framework.exception.entity.EntityBody;
import com.acmedcare.framework.newim.server.runner.loader.ServerServiceFactory;
import java.util.Collection;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

/**
 * Server Status Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-12.
 */
@Configuration
@Endpoint(id = "server-status")
public class ServerStatusEndpoint {

  @ReadOperation(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public EntityBody serverStatus() {
    Collection<String> servers = ServerServiceFactory.servers();
    return EntityBody.<Collection<String>, Void>builder().data(servers).build();
  }
}
