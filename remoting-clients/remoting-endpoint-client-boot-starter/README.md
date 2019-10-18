## Master Endpoint Spring Boot Starter

> ignore

### 更新
----

- `2.1.0-RC3`

```asciidoc
1. 新增媒体类单聊消息单发接口
2. 新增媒体类单聊消息批量接口
3. 新增媒体类单聊群消息接口

```



### 如何使用

- 添加`Maven`依赖

> 需要配置公司自己的Maven私服, 配置方法参考: [Acmedcare-Maven-Nexus](http://git.acmedcare.com:8082/acmedback/Acmedcare-Maven-Nexus)

```xml

<dependency>
    <groupId>com.acmedcare.microservices.im</groupId>
    <artifactId>spring-boot-starter-remoting-client</artifactId>
    <version>2.3.1-RC1</version>
</dependency>

```


- Spring Boot `application.properties`工程配置

```properties

# 远程服务器地址, 多个地址用`,`隔开
remoting.master.endpoint.remote-addr=192.168.1.227:13110,192.168.1.227:13120

# 是否是`https`请求
remoting.master.endpoint.https=false

# 文件服务器是否开启
remoting.nas.endpoint.enabled=true

# 文件服务器地址
remoting.nas.endpoint.remote-addr=127.0.0.1:18848

# ssl
remoting.nas.endpoint.https=false

# Nas 注册应用标识(可空)
remoting.nas.endpoint.nas-app-id=

# Nas 注册应用密钥(可空)
remoting.nas.endpoint.nas-app-key=

```


- 代码如何使用

```java
  
  // 在需要用的地方通过 `@Autowired` 进行注入
  @Autowired private MasterEndpointClient masterEndpointClient;

  // 当前版本的功能包括
  
  // 1. 创建群组
  masterEndpointClient.createNewGroup(...)

  // 2. 加群操作  
  masterEndpointClient.joinGroup(...)
  
  // 3. 删除群成员操作
  masterEndpointClient.removeGroupMembers(...)
  
  // 4. 更新群组信息
  masterEndpointClient.updateGroup(...)
  
  // 5. 删除群组操作(标记)
  masterEndpointClient.removeGroup(...)
  
  // 6. 发送单聊消息
  masterEndpointClient.sendSingleMessage(...)
  
  // 7. 批量发送单聊消息
  masterEndpointClient.batchSendSingleMessages(...)
  
  // 8. 发送群消息
  masterEndpointClient.sendGroupMessage(...)

```

### 参考案例

- [集成Starter工程](http://git.acmedcare.com:8082/acmedback/Acmedcare-NewIM/tree/2.1.x/remoting-clients/remoting-endpoint-client-boot-starter-sample)
- [Api具体操作代码](http://git.acmedcare.com:8082/acmedback/Acmedcare-NewIM/blob/2.1.x/remoting-clients/remoting-endpoint-client/src/test/java/com/acmedcare/framework/newim/master/endpoint/client/MasterEndpointClientTest.java)


