package com.acmedcare.framework.newim.master.endpoint;

import com.acmedcare.framework.newim.master.core.MasterClusterAcceptorServer;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Master Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 16/11/2018.
 */
@RestController
public class MasterEndpoint {

  private final MasterClusterAcceptorServer masterClusterAcceptorServer;

  @Autowired
  public MasterEndpoint(MasterClusterAcceptorServer masterClusterAcceptorServer) {
    this.masterClusterAcceptorServer = masterClusterAcceptorServer;
  }

  @GetMapping("/newim-master/available-servers")
  ResponseEntity availableClusterServerList() {
    try {
      Set<String> servers =
          this.masterClusterAcceptorServer.getMasterClusterSession().clusterList();
      return ResponseEntity.ok(servers);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
