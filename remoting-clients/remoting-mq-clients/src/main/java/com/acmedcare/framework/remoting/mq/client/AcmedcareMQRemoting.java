package com.acmedcare.framework.remoting.mq.client;

import static com.acmedcare.framework.remoting.mq.client.biz.BizCode.BroadcastCommand.TOPIC_MESSAGE_PUSH;

import android.content.Context;
import com.acmedcare.framework.remoting.mq.client.ServerAddressHandler.RemotingAddress;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.MonitorClient;
import com.acmedcare.framework.remoting.mq.client.biz.BizCode.SamplingClient;
import com.acmedcare.framework.remoting.mq.client.biz.request.AuthRequest;
import com.acmedcare.framework.remoting.mq.client.events.AcmedcareEvent;
import com.acmedcare.framework.remoting.mq.client.events.BasicListenerHandler;
import com.acmedcare.framework.remoting.mq.client.exception.NoServerAddressException;
import com.acmedcare.framework.remoting.mq.client.exception.SdkInitException;
import com.acmedcare.framework.remoting.mq.client.jre.JREBizExectuor;
import com.acmedcare.framework.remoting.mq.client.processor.MQTopicMessagesProcessor;
import com.acmedcare.nas.client.NasProperties;
import com.acmedcare.tiffany.framework.remoting.android.core.IoSessionEventListener;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RemotingCommand;
import com.acmedcare.tiffany.framework.remoting.android.core.protocol.RequestCode;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRClientConfig;
import com.acmedcare.tiffany.framework.remoting.android.core.xlnio.XLMRRemotingClient;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.future.IoFuture;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.future.IoFutureListener;
import com.acmedcare.tiffany.framework.remoting.android.nio.core.session.IoSession;
import com.acmedcare.tiffany.framework.remoting.android.utils.RemotingHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.Setter;

/**
 * Acmedcare+ Remoting SDK Main Class
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version alpha - 26/07/2018.
 */
public final class AcmedcareMQRemoting implements Serializable {
  @Deprecated private static final String TAG = AcmedcareMQRemoting.class.getSimpleName();

  private static final String DEFAULT_APP_ID = "Acmedcare#MQClient#SDK$ID";
  private static final String DEFAULT_APP_KEY = "Acmedcare#MQClient#SDK$KET";
  private static final AcmedcareMQRemoting INSTANCE = InstanceHolder.INSTANCE;
  private static final long serialVersionUID = -9029081624617687982L;
  private static int reConnectRetryTimes = 5; // 5 time
  private static long reConnectPeriod = 8; // 8 s
  /** Init Flag */
  private static volatile boolean inited = false;

  private static volatile boolean shutdowned = false;

  private static volatile boolean focusLogout = false;

  private static AtomicBoolean initedOnce = new AtomicBoolean(false);
  /** Running Flag */
  private static volatile boolean running = false;

  private static AtomicBoolean runOnce = new AtomicBoolean(false);
  /** Connection Flag */
  @Getter private static volatile boolean connecting = false;

  private static AsyncEventBus eventBus;
  private static ScheduledExecutorService connectWatcher;
  private static XLMRClientConfig clientConfig;
  private static RemotingParameters parameters;
  @Getter private static XLMRRemotingClient remotingClient;
  @Getter private static List<String> addresses = Lists.newArrayList();
  @Getter private static NasProperties nasProperties; // acmedcare nas config
  @Getter private volatile String currentLoginName;
  private long delay;
  /** Biz Executor Api */
  private BizExecutor bizExecutor;

  @Getter @Setter private String currentRemotingAddress;
  private RemotingConnectListener listener;
  @Getter private TopicMessageListener topicMessageListener;
  @Deprecated private IoSession remotingSession;

  private AcmedcareMQRemoting() {
    String buffer =
        "\r\n============================================================"
            + "\r\n\t\tAcmedcare MQ SDK Version    :"
            + Version.get()
            + "\r\n\t\tSDK LogCat Filter TAG       : "
            + AcmedcareLogger.SDK_LOG_TAG
            + "\r\n============================================================";
    AcmedcareLogger.i(null, buffer);
  }

  /**
   * Get Acmedcare Remoting Instance Static Method
   *
   * @return instance
   */
  public static AcmedcareMQRemoting getInstance() {
    return InstanceHolder.INSTANCE;
  }

  public BizExecutor executor() {
    return bizExecutor;
  }

  /**
   * Get Event Bus Instance
   *
   * @return event bus
   */
  public AsyncEventBus eventBus() {
    return eventBus;
  }

  public void setCurrentLoginName(String username) {
    this.currentLoginName = username;
  }

  public synchronized void init(Context context, RemotingParameters parameters) {

    if (initedOnce.get()) {
      return;
    }

    if (initedOnce.compareAndSet(false, true)) {
      AcmedcareLogger.i(TAG, "Start to init-ing remoting client.");

      if (inited) {
        AcmedcareLogger.i(TAG, "Remoting Client already init-ed. // ignore invoke.");
        return;
      }

      if (parameters != null) {
        AcmedcareMQRemoting.parameters = parameters;
      }
      inited = true;

      try {
        // build nas properties
        nasProperties = AcmedcareMQRemoting.parameters.getNasProperties();

        if (nasProperties != null) {
          if (nasProperties.getAppId() == null || nasProperties.getAppId().trim().length() == 0) {
            nasProperties.setAppId(DEFAULT_APP_ID);
          }
          if (nasProperties.getAppKey() == null || nasProperties.getAppKey().trim().length() == 0) {
            nasProperties.setAppKey(DEFAULT_APP_KEY);
          }
        }
      } catch (Exception ignore) {
      }

      // register bizExecutor
      this.bizExecutor = new JREBizExectuor(this);

      eventBus = new AsyncEventBus(Executors.newFixedThreadPool(8));

      AcmedcareLogger.i(TAG, "Remoting Client init-ed.");
    }
  }

  public boolean isMonitor() {
    return ClientType.MONITOR.equals(parameters.getClientType());
  }

  /** Register Connection Status Change Listener */
  public void registerConnectionEventListener(RemotingConnectListener listener) {
    if (this.listener == null) {
      this.listener = listener;
    }
  }

  /**
   * Active Remoting Client (first start / re-start)
   *
   * @param delay client connect delay time (unit: ms)
   * @throws NoServerAddressException no server address exception
   */
  public synchronized void run(final long delay) throws NoServerAddressException, SdkInitException {

    if (runOnce.get()) {
      return;
    }
    try {

      if (runOnce.compareAndSet(false, true)) {
        if (running) {
          AcmedcareLogger.i(TAG, "Remoting Client already running. // ignore invoke.");
          return;
        }

        running = true;

        this.delay = delay;
        assert AcmedcareMQRemoting.parameters != null
            && AcmedcareMQRemoting.parameters.getServerAddressHandler() != null;

        if (!AcmedcareMQRemoting.parameters.validate()) {
          throw new SdkInitException("SDK初始化参数异常[参考文档:" + Issues.URL + "]");
        }

        // ssl
        //        if (AcmedcareMQRemoting.parameters.isEnableSSL()) {
        //          System.setProperty("tiffany.quantum.encrypt.enable", "true");
        //        }

        // processor
        processorParameters();

        if (AcmedcareMQRemoting.remotingClient == null) {
          // random a remote address
          newRemotingClient();

          assert AcmedcareMQRemoting.remotingClient != null;
          AcmedcareMQRemoting.remotingClient.updateNameServerAddressList(
              AcmedcareMQRemoting.addresses);

          // connect
          doConnect(false);
        }
      }
    } catch (Throwable e) {
      AcmedcareLogger.e(TAG, e, "Remoting Active failed.");
      if (e instanceof NoServerAddressException) {
        throw e;
      }
    }
  }

  private void processorParameters() throws NoServerAddressException {
    // 设置重连间隔
    reConnectPeriod = AcmedcareMQRemoting.parameters.getReConnectPeriod();
    reConnectRetryTimes = AcmedcareMQRemoting.parameters.getReConnectRetryTimes();
    if (nasProperties == null) {
      nasProperties = AcmedcareMQRemoting.parameters.getNasProperties();
    }

    if (nasProperties != null) {
      if (nasProperties.getAppId() == null || nasProperties.getAppId().trim().length() == 0) {
        nasProperties.setAppId(DEFAULT_APP_ID);
      }
      if (nasProperties.getAppKey() == null || nasProperties.getAppKey().trim().length() == 0) {
        nasProperties.setAppKey(DEFAULT_APP_KEY);
      }
    }

    // address list
    List<ServerAddressHandler.RemotingAddress> masterAddresses =
        AcmedcareMQRemoting.parameters.getServerAddressHandler().remotingAddressList();

    if (masterAddresses != null && masterAddresses.size() > 0) {
      final List<String> clusterServers = Lists.newArrayList();
      for (ServerAddressHandler.RemotingAddress address : masterAddresses) {

        try {
          final RemotingAddress tempAddress = address;
          final String url =
              (address.isHttps() ? "https://" : "http://")
                  + address.toString()
                  + "/master/available-cluster-servers";
          AcmedcareLogger.i(TAG, "获取可用服务器请求地址: " + url);

          final CountDownLatch count = new CountDownLatch(1);

          Thread asyncThread =
              new Thread(
                  new Runnable() {
                    @Override
                    public void run() {

                      try {
                        URL requestURL = new URL(url);
                        HttpURLConnection urlConn = (HttpURLConnection) requestURL.openConnection();
                        urlConn.setConnectTimeout(2 * 1000);
                        urlConn.setReadTimeout(2 * 1000);
                        urlConn.setUseCaches(false);
                        urlConn.setRequestMethod("GET");
                        urlConn.connect();
                        if (urlConn.getResponseCode() == 200) {
                          String body = streamToString(urlConn.getInputStream());
                          AcmedcareLogger.i(TAG, "获取可用服务器请求返回值: " + body);
                          if (!Strings.isNullOrEmpty(body)) {
                            List<String> temp =
                                JSON.parseObject(body, new TypeReference<List<String>>() {});
                            if (temp != null && temp.size() > 0) {
                              clusterServers.addAll(temp);
                            }
                          }
                        } else {
                          AcmedcareLogger.i(
                              TAG, "获取可用服务器请求状态代码: " + urlConn.getResponseCode() + ", 尝试下一组服务器...");
                        }
                        urlConn.disconnect();

                      } catch (Exception e) {
                        e.printStackTrace();
                        AcmedcareLogger.e(
                            TAG, e, "从主服务器:" + tempAddress.toString() + "获取可用通讯服务器地址失败");
                      } finally {
                        count.countDown();
                      }
                    }
                  });
          asyncThread.start();
          count.await();

        } catch (Exception e) {
          e.printStackTrace();
          AcmedcareLogger.e(TAG, e, "异步处理网络获取通讯地址异常");
        }

        if (clusterServers.size() > 0) {
          break;
        }
      }

      AcmedcareLogger.i(TAG, " 可用通讯服务器地址:" + JSON.toJSONString(clusterServers));
      if (clusterServers.isEmpty()) {
        AcmedcareLogger.w(TAG, "无可用的通讯服务器");
        throw new NoServerAddressException();
      }

      AcmedcareMQRemoting.addresses.clear();
      AcmedcareMQRemoting.addresses.addAll(clusterServers);

      Random indexRandom = new Random();
      int index = indexRandom.nextInt(AcmedcareMQRemoting.addresses.size());
      this.currentRemotingAddress = AcmedcareMQRemoting.addresses.get(index);

      // assert address must not be null
      assert this.currentRemotingAddress != null;
    } else {
      throw new NoServerAddressException("No found remote server address .");
    }
  }

  /** Build new Remoting Client Instacne */
  private void newRemotingClient() {
    if (AcmedcareMQRemoting.clientConfig == null) {
      AcmedcareLogger.i(null, "build new client config with default setting");
      AcmedcareMQRemoting.clientConfig = new XLMRClientConfig();
      AcmedcareMQRemoting.clientConfig.setClientChannelMaxIdleTimeSeconds(40);
    }

    if (AcmedcareMQRemoting.parameters.isEnableSSL()) {
      AcmedcareMQRemoting.clientConfig.setUseTLS(true);
      AcmedcareMQRemoting.clientConfig.setJksFile(AcmedcareMQRemoting.parameters.getJksFile());
      AcmedcareMQRemoting.clientConfig.setJksPassword(
          AcmedcareMQRemoting.parameters.getJksPassword());
    }

    AcmedcareLogger.i(null, "build new remoting client instance for connect");
    AcmedcareMQRemoting.remotingClient =
        new XLMRRemotingClient(
            AcmedcareMQRemoting.clientConfig,
            new IoSessionEventListener() {
              @Override
              public void onSessionConnect(IoSession ioSession) {

                if (ioSession.isConnected()) {
                  if (AcmedcareMQRemoting.this.listener != null) {
                    AcmedcareMQRemoting.this.listener.onConnect(
                        AcmedcareMQRemoting.getRemotingClient());
                  }

                  try {
                    // 计算PING
                    final long start = System.currentTimeMillis();
                    RemotingCommand ping =
                        RemotingCommand.createRequestCommand(
                            RequestCode.SYSTEM_HEARTBEAT_CODE, null);
                    ioSession
                        .write(ping)
                        .addListener(
                            new IoFutureListener<IoFuture>() {
                              @Override
                              public void operationComplete(IoFuture future) {
                                if (future.isDone()) {
                                  AcmedcareLogger.i(
                                      null,
                                      ">>>>> Client & Remote Server Ping : "
                                          + (System.currentTimeMillis() - start)
                                          + " ms");
                                }
                              }
                            });
                  } catch (Exception e) {
                    AcmedcareLogger.w(null, "SDK Tester Ping process failed, ignore~");
                  }

                  AcmedcareMQRemoting.this.remotingSession = ioSession;
                  // auth automatic
                  AcmedcareMQRemoting.this.bizExecutor.auth(
                      AuthRequest.builder()
                          .accessToken(AcmedcareMQRemoting.parameters.getAccessToken())
                          .areaNo(AcmedcareMQRemoting.parameters.getAreaNo())
                          .orgId(AcmedcareMQRemoting.parameters.getOrgId())
                          .deviceId(AcmedcareMQRemoting.parameters.getDeviceId())
                          .username(AcmedcareMQRemoting.parameters.getUsername())
                          .passportId(AcmedcareMQRemoting.parameters.getPassportId())
                          .build(),
                      AcmedcareMQRemoting.parameters.getAuthCallback());
                }
              }

              @Override
              public void onSessionClose(IoSession ioSession) {
                if (AcmedcareMQRemoting.this.listener != null) {
                  AcmedcareMQRemoting.this.listener.onClose(
                      AcmedcareMQRemoting.getRemotingClient());
                }
                AcmedcareLogger.i(null, "Connection is closed");

                // release
                releaseResources();

                if (!AcmedcareMQRemoting.shutdowned && !focusLogout) {
                  AcmedcareLogger.i(
                      null, "Start new thread<Acmedcare-Dog-Thread> to retry connecting...");
                  new Thread(
                          new Runnable() {
                            @Override
                            public void run() {
                              AcmedcareMQRemoting.this.reConnect();
                            }
                          },
                          "Acmedcare-Dog-Thread")
                      .start();
                }
              }

              @Override
              public void onSessionException(IoSession ioSession, Throwable throwable) {
                throwable.printStackTrace();
                AcmedcareLogger.e(null, throwable, "Connection is exception ");
              }

              @Override
              public void onSessionIdle(IoSession ioSession) {
                AcmedcareLogger.i(null, "Connection is idle");
                if (AcmedcareMQRemoting.this.listener != null) {
                  AcmedcareMQRemoting.this.listener.onIdle(AcmedcareMQRemoting.getRemotingClient());
                }
              }
            });

    if (isMonitor()) {
      AcmedcareMQRemoting.remotingClient.registerProcessor(
          TOPIC_MESSAGE_PUSH, new MQTopicMessagesProcessor(this), null);
      AcmedcareLogger.i(
          null,
          "[Monitor] Register-ed MQ Topic Biz Code: 0x"
              + Integer.toHexString(TOPIC_MESSAGE_PUSH)
              + " processor.");
    }
  }

  private void doConnect(final boolean now) {
    // async start thread
    Thread startThread =
        new Thread(
            new Runnable() {
              @Override
              public void run() {
                try {
                  if (!now) {
                    Thread.sleep(delay);
                  }
                  AcmedcareLogger.i(TAG, "start to connect server..");
                  remotingClient.start();

                  // send shake hand
                  AcmedcareLogger.i(TAG, "send handshake request to connect server..");
                  handshake();

                } catch (Throwable e) {
                  AcmedcareLogger.e(TAG, e, "Async Connection Thread execute failed.");
                }
              }
            },
            "remoting-client-network-connect-thread");
    startThread.start();
  }

  private void handshake() {
    try {
      RemotingCommand handshakeRequest =
          RemotingCommand.createRequestCommand(
              parameters.getClientType().equals(ClientType.MONITOR)
                  ? MonitorClient.HANDSHAKE
                  : SamplingClient.HANDSHAKE,
              null);
      AcmedcareMQRemoting.remotingClient.invokeOneway(
          this.currentRemotingAddress, handshakeRequest, 3000);
    } catch (Exception ignore) {
    }
  }

  /** Shutdown Now */
  public void shutdownNow() {

    AcmedcareLogger.i(null, "Ready to stop sdk-remoting connection~");
    AcmedcareMQRemoting.shutdowned = true;
    // release first
    if (AcmedcareMQRemoting.remotingClient != null) {
      AcmedcareMQRemoting.remotingClient.shutdown();
    }

    // shutdownNow
    if (eventBus != null) {
      try {
        eventBus.unregister(this);
        eventBus = null;
      } catch (Exception ignore) {
      }
    }

    AcmedcareLogger.i(null, "unregister event bus ~");

    releaseResources();
    initedOnce.compareAndSet(true, false);

    AcmedcareLogger.i(null, "reset cached values settings ~");

    if (connectWatcher != null) {
      try {
        connectWatcher.shutdownNow();
      } catch (Exception e) {
      }
    }

    //
    inited = false;
    running = false;

    currentLoginName = null;
    parameters = null;
    nasProperties = null;
    bizExecutor = null;
    remotingClient = null;
    addresses.clear();
    listener = null;
    remotingSession = null;
    currentRemotingAddress = null;

    AcmedcareLogger.i(null, "release remoting's instances ~");
  }

  private void reConnect0() throws NoServerAddressException {

    AcmedcareLogger.i(TAG, "Try to re-connecting");
    // re-shutdown
    try {
      if (AcmedcareMQRemoting.remotingClient != null) {
        AcmedcareMQRemoting.remotingClient.shutdown();
      }
    } catch (Exception ignore) {
      AcmedcareLogger.w(null, "shutdown remoting client exception <ignore~> ");
    }

    // release remoting client
    AcmedcareMQRemoting.remotingClient = null;

    AcmedcareLogger.i(null, "re-process user parameters .");
    processorParameters();

    // re-new remoting client
    newRemotingClient();

    // update remoting address list
    AcmedcareMQRemoting.remotingClient.updateNameServerAddressList(AcmedcareMQRemoting.addresses);

    // connect
    try {
      AcmedcareLogger.i(TAG, "re-Connecting...");

      remotingClient.start();

      handshake();

    } catch (Exception e) {
      AcmedcareLogger.e(TAG, e, "Re-Connect Failed");
    }
  }

  /**
   * Re-Connect Api Method
   *
   * <pre>
   *
   *   <li>Only when system lose connect with remoting server ,then can invoke this method;
   *
   * </pre>
   */
  public void reConnect() {
    try {
      AcmedcareLogger.i(
          null, "Ready to re-connect to remoting server with " + reConnectRetryTimes + " times");
      if (!AcmedcareMQRemoting.connecting) {
        int times = reConnectRetryTimes;
        while (--times >= 0) {
          if (connecting) {
            AcmedcareLogger.i(null, "network is connect successed , break re-connect loop;");
            break;
          }

          try {
            try {
              AcmedcareMQRemoting.getInstance().reConnect0();
            } catch (NoServerAddressException e) {
              AcmedcareLogger.i(TAG, "This time can't get available cluster server address list.");
            }
            Thread.sleep(reConnectPeriod * 1000 + (reConnectRetryTimes - times) * 1000);
          } catch (InterruptedException ignored) {
          }
        }

        if (!AcmedcareMQRemoting.connecting) {
          AcmedcareLogger.w(
              null,
              "Retry "
                  + reConnectRetryTimes
                  + " times , network no working yet, post <RE_CONNECT_FAILED> for user to process self.");
          // ignore
          eventBus()
              .post(
                  new AcmedcareEvent() {
                    @Override
                    public Event eventType() {
                      return SystemEvent.RE_CONNECT_FAILED;
                    }

                    @Nullable
                    @Override
                    public Object data() {
                      return null;
                    }
                  });
        }
      }

    } catch (Exception e) {
      AcmedcareLogger.e(null, e, "re-connect thread execute exception");
    }
  }

  public void updateConnectStatus() {
    AcmedcareMQRemoting.connecting = true;
    AcmedcareLogger.i(null, "update connection status with : " + connecting);
    AcmedcareMQRemoting.shutdowned = false;
    AcmedcareLogger.i(null, "update connection shutdown flag with : " + shutdowned);
    AcmedcareMQRemoting.focusLogout = false;
    AcmedcareLogger.i(
        null,
        "Connect is established: ["
            + RemotingHelper.parseSocketAddressAddr(remotingSession.getLocalAddress())
            + "] -> ["
            + currentRemotingAddress
            + "]");
  }

  private void releaseResources() {
    AcmedcareLogger.i(null, "Ready to release sdk framework resouces ~");
    AcmedcareMQRemoting.connecting = false;
    runOnce.compareAndSet(true, false);
  }

  /** Register Message Handler */
  public void onMessageEventListener(BasicListenerHandler eventHandler) {
    if (eventHandler != null) {
      // register event bus handler
      eventBus().register(eventHandler);
      AcmedcareLogger.i(null, "application register event listener handler :" + eventHandler);
    }
  }

  public void registerTopicMessageListener(TopicMessageListener listener) {
    this.topicMessageListener = listener;
    AcmedcareLogger.i(null, "Topic Message Listener :" + listener);
  }

  private String streamToString(InputStream is) {
    try {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buffer = new byte[1024];
      int len = 0;
      while ((len = is.read(buffer)) != -1) {
        baos.write(buffer, 0, len);
      }
      baos.close();
      is.close();
      byte[] byteArray = baos.toByteArray();
      return new String(byteArray);
    } catch (Exception e) {
      AcmedcareLogger.e(TAG, e, e.getMessage());
      return null;
    }
  }

  /** Remoting Connection Status Listener */
  public interface RemotingConnectListener {

    /**
     * Invoke When Connect is establish;
     *
     * @param client client
     */
    void onConnect(XLMRRemotingClient client);

    /**
     * Invoked when closed;
     *
     * @param client client
     */
    void onClose(XLMRRemotingClient client);

    /**
     * Invoked When exception;
     *
     * @param client client
     */
    void onException(XLMRRemotingClient client);

    /**
     * Invoked When Idle;
     *
     * @param client client
     */
    void onIdle(XLMRRemotingClient client);
  }

  private static class InstanceHolder {
    private static AcmedcareMQRemoting INSTANCE = new AcmedcareMQRemoting();
  }
}
