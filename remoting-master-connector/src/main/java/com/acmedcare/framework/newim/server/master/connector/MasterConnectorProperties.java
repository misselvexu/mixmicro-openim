package com.acmedcare.framework.newim.server.master.connector;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MasterConnectorProperties
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "remoting.server.master")
public class MasterConnectorProperties {

  /** Master节点配置 */
  private List<String> nodes = Lists.newArrayList();

  /** 是否开启心跳,默认不开启,但是客户端会在空闲时间自动发送保活操作请求 */
  private boolean heartbeatEnabled = false;

  /** Master 心跳延迟执行时间 */
  private long heartbeatDelay = 5;

  /** Master 心跳间隔(s) */
  private long heartbeatInterval = 20;

  /** 链接延迟 */
  private long connectDelay = 5;

  /** 链接重试延迟间隔 x 重试次数 */
  private long connectionRetryDelayInterval = 5;

  /** 客户端服务地址 */
  private String connectorHost;

  /** 客户端服务端口 */
  private int connectorPort;

  /** 客户端 Replica 通讯端口 */
  private int connectorReplicaPort;

  /** 链接检查间隔 */
  private long connectionCheckPeriod = 10;

  /** 客户端请求超时时间(ms) */
  private long connectorRequestTimeout = 5000;

  /** 请求失败,最大重试次数 */
  private int connectorRequestMaxRetryTimes = 2;

  /** 请求失败,重试延迟间隔(ms) */
  private long connectorRequestRetryPeriod = 3000;

  /** 空闲时间(s) */
  private long connectorIdleTime = 60;

  /** 延迟拉取备份节点列表时间 */
  private long connectorClusterReplicaRollingPullDelay = 10;

  /** 拉取备份节点列表时间间隔 */
  private long connectorClusterReplicaRollingPullPeriod = 30;

  /** 客户端上报节点链接数据延迟时间 */
  private long connectorClusterChannelsSyncDelay = 10;

  /** 客户端上报节点链接数据时间间隔 */
  private long connectorClusterChannelsSyncPeriod = 20;

  /** 客户端的命名空间 */
  private String connectorNamespace = "DEFAULT";

  /** 客户端是否开启 SSL */
  private boolean connectorEnableTls = false;

  /** 客户端链接事件监听器 */
  private String connectorChannelEventListener;
}
