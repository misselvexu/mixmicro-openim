package com.acmedcare.framework.newim.server.master.connector;

import com.acmedcare.framework.kits.Assert;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.ExecutorCallback;
import com.acmedcare.framework.kits.executor.RetriableThreadExecutor.RetriableAttribute;
import com.acmedcare.framework.kits.thread.ThreadKit;
import com.acmedcare.framework.newim.BizResult;
import com.acmedcare.framework.newim.protocol.Command.MasterClusterCommand;
import com.acmedcare.framework.newim.protocol.request.ClusterRegisterHeader;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingConnectException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingSendRequestException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTimeoutException;
import com.acmedcare.tiffany.framework.remoting.exception.RemotingTooMuchRequestException;
import com.acmedcare.tiffany.framework.remoting.netty.NettyClientConfig;
import com.acmedcare.tiffany.framework.remoting.netty.NettyRemotingSocketClient;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.protocol.RemotingSysRequestCode;
import com.alibaba.fastjson.JSON;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MasterInstance
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2018-12-17.
 */
public final class MasterInstance {

  private static final Logger logger = LoggerFactory.getLogger(MasterInstance.class);
  private AtomicBoolean startup = new AtomicBoolean(false);
  private AtomicInteger connectTimes = new AtomicInteger(1);
  private ScheduledExecutorService heartbeatExecutor;
  private ScheduledExecutorService connectExecutor;

  /** Master Host */
  @Getter private String host;
  /** Master Port */
  @Getter private int port;

  @Getter private NettyRemotingSocketClient client;
  private NettyClientConfig config;
  private MasterConnectorContext context;
  private MasterConnectorProperties properties;
  @Getter private volatile boolean connected = false;

  private MasterInstance() {}

  private MasterInstance(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Create new master instance with address
   *
   * @param nodeAddress address , like : 192.168.1.1:8080
   * @return a instance of {@link MasterInstance}
   */
  static MasterInstance newInstance(String nodeAddress) {
    String[] temp = nodeAddress.split(":");
    if (temp.length != 2) {
      throw new IllegalArgumentException("invalid master node address param , sample: [host:port]");
    }
    return new MasterInstance(temp[0], Integer.parseInt(temp[1]));
  }

  void registerClientInstance(
      MasterConnectorContext context,
      MasterConnectorProperties properties,
      NettyClientConfig config,
      NettyRemotingSocketClient client) {
    this.context = context;
    this.properties = properties;
    this.config = config;
    this.client = client;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MasterInstance)) {
      return false;
    }
    MasterInstance that = (MasterInstance) o;
    return getPort() == that.getPort() && getHost().equals(that.getHost());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getHost(), getPort());
  }

  @Override
  public String toString() {
    return "MasterInstance{" + "host='" + host + '\'' + ", port=" + port + '}';
  }

  String serverAddress() {
    return this.host + ":" + this.port;
  }

  void startup(CountDownLatch latch) {
    try {
      // START UP BIZ
      if (startup.compareAndSet(false, true)) {
        if (this.client != null) {

          logger.info("starting connector client ....");
          this.client.start();

          if (connectExecutor == null) {
            connectExecutor =
                new ScheduledThreadPoolExecutor(
                    1, new DefaultThreadFactory("master-connector-connection-thread"));
            connectExecutor.scheduleWithFixedDelay(
                () -> {
                  try {
                    if (!connected) {
                      ThreadKit.sleep(
                          this.properties.getConnectionRetryDelayInterval() * connectTimes.get(),
                          TimeUnit.SECONDS);
                      doConnect();
                    } // ok
                  } catch (Throwable e) {
                    logger.warn(
                        "Connect to master server:{} failed, wait next time connect..",
                        serverAddress());
                    connectTimes.incrementAndGet();
                  }
                },
                this.properties.getConnectDelay(),
                this.properties.getConnectionCheckPeriod(),
                TimeUnit.SECONDS);
            logger.info(
                "starting up connector executor , init-delay:{} ,fixed-period :{}",
                this.properties.getConnectDelay(),
                this.properties.getConnectionCheckPeriod());
          }
        }
      }

    } finally {
      latch.countDown();
    }
  }

  private void doConnect() {
    try {
      handshake();
    } catch (Exception e) {
      logger.error("Master-Cluster-Client connect exception", e);
    }
  }

  private void resetConnectParams() {
    connectTimes.set(1);
  }

  private void handshake() throws Exception {
    CountDownLatch count = new CountDownLatch(1);
    logger.info("send handshake request to server :{} ", serverAddress());
    RemotingCommand handshakeRequest =
        RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_HANDSHAKE, null);
    client.invokeOneway(
        serverAddress(),
        handshakeRequest,
        this.properties.getConnectorRequestTimeout(),
        b -> {
          if (b) {
            logger.info("send handshake request succeed, then send register request ...");

            RetriableThreadExecutor<Void> retriableThreadExecutor =
                new RetriableThreadExecutor<>(
                    "Register-Retry-Thread",
                    () -> {
                      register();
                      return null;
                    },
                    new RetriableAttribute(
                        this.properties.getConnectorRequestMaxRetryTimes(),
                        this.properties.getConnectorRequestRetryPeriod(),
                        TimeUnit.MILLISECONDS),
                    new ExecutorCallback<Void>() {
                      @Override
                      public void onCompleted(Void result) {
                        count.countDown();
                      }

                      @Override
                      public void onFailed(String message) {
                        count.countDown();
                      }
                    });

            retriableThreadExecutor.execute();
          }
        });

    count.await();
  }

  private void register() throws Exception {
    Object body = this.context.getWssEndpoints();

    ClusterRegisterHeader header = new ClusterRegisterHeader();
    header.setNodeServerType(this.properties.getConnectorType().name());
    header.setNodeServerHost(this.properties.getConnectorHost() + ":" + this.properties.getConnectorPort());
    header.setNodeServerAddress(this.properties.getConnectorHost() + ":" + this.properties.getConnectorReplicaPort());

    header.setHasWssEndpoints(body != null);

    // send register command
    logger.info("send register request to server :{} ", serverAddress());

    RemotingCommand registerRequest =
        RemotingCommand.createRequestCommand(MasterClusterCommand.CLUSTER_REGISTER, header);

    if (body != null) {
      registerRequest.setBody(JSON.toJSONBytes(body));
    }

    RemotingCommand response =
        client.invokeSync(
            serverAddress(), registerRequest, this.properties.getConnectorRequestTimeout());

    Assert.notNull(response, "register response must not be null.");
    BizResult bizResult = JSON.parseObject(response.getBody(), BizResult.class);
    if (bizResult != null && bizResult.getCode() == 0) {
      logger.info("Master-Cluster-Client:{} register succeed. ", serverAddress());

      connected = true;
      // reset connect params
      resetConnectParams();

      // heartbeat startup
      if (this.properties.isHeartbeatEnabled()) {
        heartbeat();
      }
    }
  }

  private void heartbeat() {
    if (heartbeatExecutor == null) {
      logger.info("startup master client heartbeat schedule thread.");
      // startup heartbeat
      heartbeatExecutor =
          new ScheduledThreadPoolExecutor(
              1, new DefaultThreadFactory("master-connector-heartbeat-thread"));

      heartbeatExecutor.scheduleWithFixedDelay(
          () -> {
            RemotingCommand heartbeat =
                RemotingCommand.createRequestCommand(RemotingSysRequestCode.HEARTBEAT, null);
            try {
              if (connected) {
                client.invokeOneway(
                    serverAddress(),
                    heartbeat,
                    this.properties.getConnectorRequestTimeout(),
                    b -> {
                      if (b) {
                        if (logger.isDebugEnabled()) {
                          logger.debug("master connector send heartbeat succeed.");
                        }
                      }
                    });
              }
            } catch (InterruptedException
                | RemotingTooMuchRequestException
                | RemotingSendRequestException
                | RemotingTimeoutException
                | RemotingConnectException e) {
              logger.warn(
                  "heartbeat thread interrupted exception or too much request exception or lose connection or request timeout , ignore");

              if (e instanceof RemotingConnectException) {
                connected = false;
                logger.warn("remote master connection is lose, waiting for re-connect ...");
              }
            }
          },
          this.properties.getHeartbeatDelay(),
          this.properties.getHeartbeatInterval(),
          TimeUnit.SECONDS);
    }
  }

  void shutdown() {
    if (heartbeatExecutor != null) {
      ThreadKit.gracefulShutdown(heartbeatExecutor, 5, 10, TimeUnit.SECONDS);
    }

    if (connectExecutor != null) {
      ThreadKit.gracefulShutdown(connectExecutor, 5, 10, TimeUnit.SECONDS);
    }
    if (client != null) {
      this.client.shutdown();
    }
  }
}
