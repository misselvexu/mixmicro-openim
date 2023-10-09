package com.acmedcare.framework.newim.master.endpoint;

import com.acmedcare.framework.exception.defined.InvalidRequestParamException;
import com.acmedcare.framework.kits.StringUtils;
import com.acmedcare.framework.kits.Strings;
import com.acmedcare.framework.newim.InstanceType;
import com.acmedcare.framework.newim.master.core.MasterClusterAcceptorServer;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterBody.WssInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

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

  public MasterEndpoint(MasterClusterAcceptorServer masterClusterAcceptorServer) {
    this.masterClusterAcceptorServer = masterClusterAcceptorServer;
  }

  @GetMapping("/available-cluster-servers")
  ResponseEntity availableClusterServerList(
      @RequestParam(required = false, defaultValue = "default") String type,
      @RequestParam(required = false, defaultValue = "default") String zone) {
    try {
      InstanceType instanceType = InstanceType.DEFAULT;
      try {
        if (StringUtils.isNotBlank(type)) {
          instanceType = InstanceType.valueOf(type.toUpperCase());
        }
      } catch (Exception e) {
        throw new InvalidRequestParamException("无效的服务类型[DEFAULT,MQ...]");
      }
      Set<String> servers =
          this.masterClusterAcceptorServer
              .getMasterClusterSession()
              .clusterList(instanceType, zone);
      return ResponseEntity.ok(servers);
    } catch (InvalidRequestParamException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @GetMapping("/available-wss-servers")
  ResponseEntity availableWssServerList(
      @RequestParam String wssName,
      @RequestParam(required = false, defaultValue = "default") String zone) {
    try {

      if (!Strings.hasLength(wssName)) {
        return ResponseEntity.badRequest().body("请求参数WebSocket服务名称不能为空");
      }

      List<WssInstance> wssInstances =
          this.masterClusterAcceptorServer.getMasterClusterSession().wssList(zone);

      wssInstances.removeIf(wssInstance -> !wssInstance.getWssName().equals(wssName));

      return ResponseEntity.ok(wssInstances);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
