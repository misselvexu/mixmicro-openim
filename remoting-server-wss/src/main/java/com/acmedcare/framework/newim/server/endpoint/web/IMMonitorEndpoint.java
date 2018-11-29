package com.acmedcare.framework.newim.server.endpoint.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * IM Monitor Endpoint
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 09/11/2018.
 */
@RestController
@RequestMapping("/im-monitor")
public class IMMonitorEndpoint {

  /**
   * 查询区域机构在线的账号设备
   *
   * @param areaNo 区域编号
   * @param orgId 机构编号
   * @return 列表
   */
  @GetMapping("/passport/online-list/")
  ResponseEntity<?> onlinePassports(@RequestParam String areaNo, @RequestParam String orgId) {

    // TODO

    return null;
  }

  /**
   * 查询分站调度客户端在线列表
   *
   * @param orgId 主站机构编号
   * @return 列表
   */
  @GetMapping("/web-client/online-list")
  ResponseEntity<?> queryOnlineScheduleSubClients(@RequestParam String orgId) {

    // TODO
    return null;
  }
}
