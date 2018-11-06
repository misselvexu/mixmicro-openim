## Acmedcare+ IM System

Acmedcare Bass System IM Server & Develop Client SDK Support.

### How to Build

```bash
    # clone source code
    git clone git@115.29.47.72:acmedback/acmedcare-im.git
    cd acmedcare-im
    # run package
    mvn clean package

```
### How to Start Server
```bash
    
    tar -zxvf remoting-server-2.1.0-RC1-assembly.tar.gz
    cd remoting-server-2.1.0-RC1
    
    # start server
    ./bin/start.sh
    
    # stop server
    ./bin/stop.sh
    
    # restart server
    ./bin/restart.sh
    
    # cat log
    tail -f logs/stdout.log 
```

### `Protocol` Biz Code

> 业务编码

| 协议说明 | Code | Value |
| --- | --- | --- |
| 授权认证 | AUTH | 0x2001 |
| 客户端拉取消息 | CLIENT_PULL_MESSAGE | 0x3001 |
| 客户端拉取群组列表 | CLIENT_PULL_OWNER_GROUPS | 0x3002 |
| 客户端拉取会话列表 | CLIENT_PULL_OWNER_SESSIONS | 0x3003 |
| 客户端推送消息已读状态 | CLIENT_PUSH_MESSAGE_READ_STATUS | 0x3004 |
| 客户端拉取会话状态 | CLIENT_PULL_SESSION_STATUS | 0x3004 |
| 客户端发消息 | CLIENT_PUSH_MESSAGE | 0x3005 |
| 客户端接收服务端消息 | SERVER_PUSH_MESSAGE | 0x3006 |

> 系统编码

| 协议说明 | Code | Value |
| --- | --- | --- |
| 心跳协议 | HEARTBEAT | 0x1001 |
| ACK(default) | PONG | 0x0000 |

### Development SDK

#### Android

* JDK 1.7+
* Android Studio 3.0+ [`optional`]

> Android SDK Source [Acmedcare Gitlab](http://115.29.47.72:8082/acmedback/tiffany-quantum/tree/Oceania.SR1/remoting-android-library-jre-core)


##### Android SDK Dependencies

| 依赖库名 | 地址 | 版本 |
| --- | --- | --- |
| remoting-android-jarlib-nio | [Acmedcare Gitlab](http://115.29.47.72:8082/acmedback/tiffany-quantum/tree/Oceania.SR1/remoting-android-jarlib-nio) | Oceania.SR1 |
| fastjson | - | 1.1.68.android |
| slf4j | - | 1.7.25 |

#### iOS

> coming soon

#### C++

> coming soon

### 依赖库

| 依赖库名 | 地址 | 版本 |
| --- | --- | --- |
| tiffany-quantum | [Acmedcare Gitlab](http://115.29.47.72:8082/acmedback/tiffany-quantum) | Oceania.SR1 |


