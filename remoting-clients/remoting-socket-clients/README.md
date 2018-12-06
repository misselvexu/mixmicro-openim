## Acmedcare+ Remoting SDK For Java

### Sample

> 测试服务器地址

- 192.168.1.227:13110
- 192.168.1.227:13120

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
 
> Usage 

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
                
            // 开启TLS加密处理
            .enableSSL(true)
            // jks如果不传递, SDK使用默认证书
            .jksFile(
                new File(
                    "/path/of/keystore.jks"))
            .jksPassword("1qaz2wsx")
            // 开启文件服务器的支持
            .nasProperties(nasProperties)
            .username(KnownParams.passport)
            .accessToken(KnownParams.accessToken)
            .areaNo(KnownParams.areaNo)
            .orgId(KnownParams.orgId)
            .passportId(KnownParams.passportId)
            .deviceId(KnownParams.deviceId)
            .heartbeatPeriod(10)
            .serverAddressHandler(
                () ->
                    Lists.newArrayList(
                        new RemotingAddress(false, "192.168.1.227", 13110, false),
                        new RemotingAddress(false, "192.168.1.227", 13120, false)))
            .build();

    // 初始化
    AcmedcareRemoting.getInstance().init(null, temp);


    // 注册监听
    AcmedcareRemoting.getInstance()
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

    // 注销消息监听
    AcmedcareRemoting.getInstance()
        .onMessageEventListener(
            new BasicListenerHandler() {
              @Override
              public void execute(AcmedcareEvent acmedcareEvent) {
                Event event = acmedcareEvent.eventType();
                Object data = acmedcareEvent.data();

                System.out.println(event);
                System.out.println(JSON.toJSONString(data));
              }
            });

    try {
      // 启动
      AcmedcareRemoting.getInstance().run(1000);
    } catch (NoServerAddressException e) {
      //TODO 无可用的服务器地址异常 客户端自行处理
      e.printStackTrace();
    } catch (SdkInitException e) {
      //TODO SDK初始化异常 客户端自行处理
      e.printStackTrace();
    }


```