## 解决IM服务器接收消息不正常的情况


### 重新替换Jar包

> 包目录在 [fixed-libs](http://115.29.47.72:8082/acmedback/acmedcare-im/tree/master/fixed-libs)

* remoting-android-jarlib-nio-Oceania.SR1.jar       `Nio Socket 底层包`
* remoting-android-library-jre-core-Oceania.SR1.jar   `Android SDK Jar包`
* remoting-server-2.1.0-RC1.jar              `替换服务端192.168.1.4的服务端包`


### 服务器部署重启

#### 测试服务器`root`账号

```bash
 ssh root@192.168.1.4
 # 密码: acmed123

```

```bash
  
  cd /opt/remoting-server-2.1.0-RC1
  
  #将`remoting-server-2.1.0-RC1.jar` 覆盖替换到lib目录下面
  
  
  # 重启服务即可
  ./bin/restart.sh
 
```

### 切换账号时`SDK`初始化的正确姿势


> SDK 初始化的基本步骤如下 

```java
  // 1.初始化各项参数
  RemotingParameters temp =
          RemotingParameters.builder()
              .authCallback(
                  new AuthCallback() {
                    @Override
                    public void onSuccess() {}
  
                    @Override
                    public void onFailed(int code, String message) {}
                  })
              .username(username)
              .heartbeatPeriod(10)
              .serverAddressHandler(
                  new ServerAddressHandler() {
                    @Override
                    public List<RemotingAddress> remotingAddressList() {
                      return Lists.newArrayList(new RemotingAddress("127.0.0.1", 8887, false));
                    }
                  })
              .build();
  
      // 2.调用客户端的初始化方法
      AcmedcareRemoting.getInstance().init(null, temp);
  
      // 3. 注册客户端的网络事件监听
      AcmedcareRemoting.getInstance()
          .registerConnectionEventListener(
              new RemotingConnectListener() {
                @Override
                public void onConnect(XLMRRemotingClient client) {
                  System.out.println("onConnect");
                }
  
                @Override
                public void onClose(XLMRRemotingClient client) {
                  System.out.println("onClose");
                }
  
                @Override
                public void onException(XLMRRemotingClient client) {
                  System.out.println("onException");
                }
  
                @Override
                public void onIdle(XLMRRemotingClient client) {
                  System.out.println("onIdle");
                }
              });
      
      // 4. 注册消息事件监听
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
        
        // 5. 开启连接方法
        AcmedcareRemoting.getInstance().run(1000);
  
      } catch (NoServerAddressException e) {
        e.printStackTrace();
      }

```

> 切换账号如何处理?

* 切换账号之前调用

```java 
  AcmedcareRemoting.getInstance().shutdownNow();
```

* 然后重新操作上面的五个步骤`建议客户端进行方法封装处理`

> Demo

```java
  
  static void connectToImServerWithUsername(String username) {
  
    // 1. ....
    // 2. ....
    // 3. ....
    // 4. ....
    // 5. ....
  
  } 
  
  
  static void changeAccountLogin() {
    
    // 1. shutdown remoting client
    AcmedcareRemoting.getInstance().shutdownNow();
    
    // 3. Re-Login With Username
    connectToImServerWithUsername("another-username");
  
  }
  

```

