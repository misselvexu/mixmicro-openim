## 通讯客户端 SDK 升级方案

1. 添加新的`sdk jar`依赖到对应的客户端工程
2. 修改 SDK 初始化代码程序

```java

    RemotingParameters temp =
        RemotingParameters.builder()
            // 启用 ssl
            .enableSSL(true)
            //
            .username("demo")
            .heartbeatPeriod(10)
            .serverAddressHandler(
                new ServerAddressHandler() {
                  @Override
                  public List<RemotingAddress> remotingAddressList() {
                    return Lists.newArrayList(new RemotingAddress("192.168.1.4", 8887, false));
                  }
                })
            .build();

```