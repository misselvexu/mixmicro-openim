package com.acmedcare.framework.newim.master.endpoint;

import com.acmedcare.framework.kits.Strings;
import com.acmedcare.framework.newim.master.core.MasterClusterAcceptorServer;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterBody.WssInstance;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Master Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 16/11/2018.
 */
@RestController
@RequestMapping("/master")
public class MasterEndpoint {

  private final MasterClusterAcceptorServer masterClusterAcceptorServer;

  @Autowired
  public MasterEndpoint(MasterClusterAcceptorServer masterClusterAcceptorServer) {
    this.masterClusterAcceptorServer = masterClusterAcceptorServer;
  }

  @GetMapping("/available-cluster-servers")
  ResponseEntity availableClusterServerList() {
    try {
      Set<String> servers =
          this.masterClusterAcceptorServer.getMasterClusterSession().clusterList();
      return ResponseEntity.ok(servers);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/available-wss-servers")
  ResponseEntity availableWssServerList(@RequestParam String wssName) {
    try {

      if (!Strings.hasLength(wssName)) {
        return ResponseEntity.badRequest().body("请求参数WebSocket服务名称不能为空");
      }

      List<WssInstance> wssInstances =
          this.masterClusterAcceptorServer.getMasterClusterSession().wssList();

      wssInstances.removeIf(wssInstance -> !wssInstance.getWssName().equals(wssName));

      return ResponseEntity.ok(wssInstances);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
