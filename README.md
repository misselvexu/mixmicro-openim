# Acmedcare+ NewIM System
---
Acmedcare NewIM System , Support Some New Features

## RoadMap

> IM Server

- Protocol Support
    * [x] TCP
    * [x] R-UDP
    * [x] WebSocket
    
- Develop Kit
    * [x] JDK SDK Kit
    * [x] Android SDK Kit
    * [ ] NodeJS SDK Kit
    
> Push Server

- Protocol Support

    * [ ] R-UDP

<<<<<<< HEAD
| 协议说明 | Code | Value |
| --- | --- | --- |
| 授权认证 | AUTH | 0x2001 |
| 客户端拉取消息 | CLIENT_PULL_MESSAGE | 0x3001 |
| 客户端拉取群组列表 | CLIENT_PULL_OWNER_GROUPS | 0x3002 |
| 客户端拉取会话列表 | CLIENT_PULL_OWNER_SESSIONS | 0x3003 |
| 客户端推送消息已读状态 | CLIENT_PUSH_MESSAGE_READ_STATUS | 0x3004 |
| 客户端拉取会话状态 | CLIENT_PULL_SESSION_STATUS | 0x3005 |
| 客户端发消息 | CLIENT_PUSH_MESSAGE | 0x3006 |
| 客户端接收服务端消息 | SERVER_PUSH_MESSAGE | 0x4001 |
| 服务端通知客户端下线通知 | SERVER_PUSH_FOCUS_LOGOUT | 0x4002 |
=======
- Develop Kit
    * [x] JDK SDK Kit
    * [x] Android SDK Kit
>>>>>>> origin/2.1.x


## Quick-start
> building

## Core Library

### Storage Component

#### MongoDB

Document storage database;

***WARN*** `Mongo4.x Single Cluster` not support transaction.


> Docker Running

```bash
  # pull images
  docker pull mongo
  
  # start container
  docker run -p 27017:27017 -v /acmedcare/data/mongo:/data/db --name docker_mongodb -d mongo
  
```

> Docker For Mongo Replica Env

```bash
  # 创建数据目录
  mkdir /acmedcare/replica-datas/cluster{1..3} -pv
  
  


```

#### MySQL (`Removed`)
The MySQL v1 component currently is only tested with MySQL 5.6-7. It is designed to be easy to understand, and get started with. For example, it deconstructs spans into columns, so you can perform ad-hoc queries using SQL. However, this component has known performance issues: queries will eventually take seconds to return if you put a lot of data into it.



### Running the server from source
> building


## Artifacts
> building