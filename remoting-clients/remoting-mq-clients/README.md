## Acmedcare+ MQ Remoting SDK For Java

### Sample

> 测试服务器地址

- 192.168.1.227:13110
- 192.168.1.227:13120
- 192.168.1.227:13130

> 支持客户端类型

```java

public enum ClientType {

  /** 监测端 */
  MONITOR,

  /** 采集端 */
  SAMPLING
}


```

> 已知前置条件参数

```java

// 登录Token
String accessToken =
        "eyJhbGciOiJSUzI1NiJ9.eyJfaWQiOiJjMTZjZDk5MzM4NDY0YmQ5OWExMGRkOWFmYWNiN2VhNiIsImRhdCI6Ik4vQmtqTkJBelh0Y04rZDdKRExrVU5OOWNXU2JQWDlId29hV0RYN1B1UElzZ1BSMlNvbS9JK09kWWpWK0hJS0pwWG9ja2Vvb1o3eVZ4a0YydnZweDJtTHA1YVJrOE5FanZrZyszbU8rZXczNmpoaEFkQ1YvVFhhTWNKQ1lqZDhCd1YrMW13T1pVdjJPVzhGZ2tPOERKVmo5bWhKeDMxZ0tIMUdPdmowanA4ST0iLCJpYXQiOjE1NDMzMTEyNDcxMTgsImV4cCI6MTU0MzkyNDUyMTExOCwiYXVkIjpudWxsfQ.BhrmV4LBkhyifVKvHqdImLZ-ppRsU9nmM09fHw9-Zjycz0x9Sqi7MjIYsYo1F_DbUIXNeKLo9KxaBmyyefR-zIMTBA2X4irGIb7e9TQU0zz7tihdxtqi0epKNOAKN_3rsMaoCI9YQeJTw5hqWOIBWnFalZMC1jvSRLuSlZvD-fwEJFBQTu6dQ0IgFijZApoeGEo0_bDn7TKPeyL7s8I1Wm1V_vPc297d1O1xj0PzLoH346_G3U1qjt6SI6pYUfV26TzS20m4sAcgZbDn10wOyMUketjgEEKefahnSM41AgohvE8z0fAaVUASq1DFaMiJeMKARgewSBySYFTS-ImyjA";

// 区域编号
String areaNo = "320500";

// 机构编号
String orgId = "3910249034228736";

// 通行证编号
Long passportId = 3837142362366976L;

// 通行证
String passport = "13910187669";

// 设备编号
String deviceId = "DEVICE-ID";

```
 
### Usage 

#### 初始化

```java

    // Android 默认不开启此选项
    // System.setProperty(AcmedcareLogger.NON_ANDROID_FLAG, "true");
    
    // 设置请求超时时间, 单位毫秒
    // System.setProperty("tiffany.quantum.request.timeout", 5000);

    // Nas 文件服务器配置
    NasProperties nasProperties = new NasProperties();
    nasProperties.setServerAddrs(Lists.<String>newArrayList("192.168.1.226:18848"));
    nasProperties.setHttps(false);

    // SDK初始化参数对象
    RemotingParameters temp =
            RemotingParameters.builder()
                .authCallback(
                    new AuthCallback() {
                      @Override
                      public void onSuccess() {
                        System.out.println("授权成功");
                      }
    
                      @Override
                      public void onFailed(int code, String message) {
                        System.out.println("授权失败,Code=" + code + ", 错误:" + message);
                      }
                    })
                .enableSSL(true)
                .jksFile(
                    new File(
                        "/Users/ive/git-acmedcare/Acmedcare-NewIM/remoting-certs/client/keystore.jks"))
                .jksPassword("1qaz2wsx")
                .username(KnownParams.passport)
                .accessToken(KnownParams.accessToken)
                .areaNo(KnownParams.areaNo)
                .orgId(KnownParams.orgId)
                .passportId(KnownParams.passportId)
                .deviceId(KnownParams.deviceId)
                .heartbeatPeriod(10)
                .clientType(ClientType.SAMPLING)   // 客户端类别标记 (采集端/监控端)
                .serverAddressHandler(
                    new ServerAddressHandler() {
                      @Override
                      public List<RemotingAddress> remotingAddressList() {
                        return Lists.newArrayList(
                            new RemotingAddress(false, "192.168.1.227", 13110, false));
                      }
                    })
                .build();
    
        AcmedcareMQRemoting.getInstance().init(null, temp);
    
        AcmedcareMQRemoting.getInstance()
            .registerConnectionEventListener(
                new RemotingConnectListener() {
                  @Override
                  public void onConnect(XLMRRemotingClient client) {
                    System.out.println("连接已打开");
                  }
    
                  @Override
                  public void onClose(XLMRRemotingClient client) {
                    System.out.println("连接断开,等待 SDK 重连");
                  }
    
                  @Override
                  public void onException(XLMRRemotingClient client) {}
    
                  @Override
                  public void onIdle(XLMRRemotingClient client) {}
                });
    
        AcmedcareMQRemoting.getInstance()
            .onMessageEventListener(
                new BasicListenerHandler() {
                  @Override
                  public void execute(AcmedcareEvent acmedcareEvent) {
                    Event event = acmedcareEvent.eventType();
                    Object data = acmedcareEvent.data();
                    System.out.println("接收到事件:" + event + " ,数据: " + JSON.toJSONString(data));
                  }
                });
    
        // 接收到主题订阅消息的推送回调
        AcmedcareMQRemoting.getInstance()
            .registerTopicMessageListener(
                new TopicMessageListener() {
                  @Override
                  public ConsumeResult onMessages(List<Message> messages) {
                    System.out.println("接收到主题消息: " + JSON.toJSONString(messages));
                    return ConsumeResult.CONSUMED;
                  }
                });
    
        try {
          AcmedcareMQRemoting.getInstance().run(1000);
    
        } catch (NoServerAddressException | SdkInitException e) {
          e.printStackTrace(); // 异常自行处理
        }

```

#### 功能实现

```java

/**
   * 发送主题消息
   *
   * @param topicId 主题ID
   * @param topicTag 主题标识
   * @param message 消息内容
   */
  static void sendTopicMessage(
      String passport, String passportId, long topicId, String topicTag, String message) {

    SendTopicMessageRequest request = new SendTopicMessageRequest();
    request.setTopicTag(topicTag);
    request.setTopicId(topicId);
    request.setContent(message.getBytes());
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .sendTopicMessage(
            request,
            new Callback() {
              @Override
              public void onSuccess(Long messageId) {}

              @Override
              public void onFailed(int code, String message) {}

              @Override
              public void onException(BizException e) {}
            });
  }

  /**
   * 创建主题
   *
   * @param topicName 主题名
   * @param topicTag 主题标识
   */
  static void createTopic(String passport, String passportId, String topicName, String topicTag) {
    NewTopicRequest request = new NewTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicName(topicName);
    request.setTopicTag(topicTag);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .createNewTopic(
            request,
            new NewTopicRequest.Callback() {
              @Override
              public void onSuccess(Long topicId) {
                System.out.println("主题创建成功,返回值:" + topicId);
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 拉取主题订阅关系
   *
   * @param passport
   * @param passportId
   * @param topicId
   */
  static void pullTopicSubscribeMappings(String passport, String passportId, String topicId) {
    PullTopicSubscribedMappingsRequest request = new PullTopicSubscribedMappingsRequest();
    request.setTopicId(topicId);
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .pullTopicSubscribedMappings(
            request,
            new PullTopicSubscribedMappingsRequest.Callback() {
              @Override
              public void onSuccess(List<TopicMapping> mappings) {
                System.out.println("返回值:" + JSON.toJSONString(mappings));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 拉取主题列表
   *
   * @param passport
   * @param passportId
   */
  static void pullTopics(String passport, String passportId) {
    PullTopicListRequest request = new PullTopicListRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);

    AcmedcareMQRemoting.getInstance()
        .executor()
        .pullAllTopics(
            request,
            new PullTopicListRequest.Callback() {

              @Override
              public void onSuccess(List<Topic> topics) {
                System.out.println("返回值:" + JSON.toJSONString(topics));
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 订阅主题
   *
   * @param topicIds 主题编号
   */
  static void subscribeTopic(String passport, String passportId, String topicIds) {
    SubscribeTopicRequest request = new SubscribeTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicIds(topicIds.split(","));

    AcmedcareMQRemoting.getInstance()
        .executor()
        .subscribe(
            request,
            new SubscribeTopicRequest.Callback() {
              @Override
              public void onSuccess() {
                System.out.println("订阅成功");
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * 取消订阅主题
   *
   * @param topicIds 主题编号
   */
  static void unSubscribeTopic(String passport, String passportId, String topicIds) {
    SubscribeTopicRequest request = new SubscribeTopicRequest();
    request.setPassport(passport);
    request.setPassportId(passportId);
    request.setTopicIds(topicIds.split(","));

    AcmedcareMQRemoting.getInstance()
        .executor()
        .unsubscribe(
            request,
            new SubscribeTopicRequest.Callback() {
              @Override
              public void onSuccess() {
                System.out.println("取消订阅成功");
              }

              @Override
              public void onFailed(int code, String message) {
                System.out.println("失败,code = " + code + " , message = " + message);
              }

              @Override
              public void onException(BizException e) {
                e.printStackTrace();
              }
            });
  }



```