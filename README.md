# 🥳 Acmedcare+ OpenIM
---
Acmedcare OpenIM is a distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability

It offers a variety of features:

- [x] Group & Topic message model
- [x] Pub/Sub messaging model
- [x] Message retroactivity by time or offset
- [x] Flexible distributed scale-out deployment architecture
- [x] Lightning-fast batch message exchange system
- [x] Efficient pull&push consumption model
- [x] Docker images for isolated testing and cloud isolated clusters
- [ ] Scheduled message delivery
- [ ] Log collection for streaming
- [ ] Big data integration
- [x] Reliable FIFO and strict ordered messaging in the same queue
- [x] Million-level message accumulation capacity in a single queue
- [ ] Feature-rich administrative dashboard for configuration, metrics and monitoring

## RoadMap

### Architecture

<img src="doc/architecture.jpg"/>

### Master Server
Naming server , support client load-balance ,query & execute endpoints; 

### Cluster Server

- Protocol Support
    * [x] TCP
    * [x] R-UDP
    * [x] WebSocket
    
- Develop Kit
    * [x] JDK SDK Kit
    * [x] Android SDK Kit
    * [ ] NodeJS SDK Kit
    
### Push Server

- Protocol Support

    * [ ] R-UDP

- Develop Kit
    * [x] JDK SDK Kit
    * [x] Android SDK Kit


### Message Queue Server
Message Queue Server ,support topic ,queue ...

> Endpoint Client

Development Kit for application to use master endpoint , 
like:
- Group Management
- Group Member Refs Management
- Send Message Apis
- Media Message Api


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

> Deploy MongoDB CLuster Replica

```bash
  # 创建数据目录
  mkdir /acmedcare/replica-datas/cluster{1..3} -pv
  ...
  ...

```

Detail @See [MongoDB Development Doc](mongo-configs/README.md)

#### MySQL (`Removed`)
The MySQL v1 component currently is only tested with MySQL 5.6-7. It is designed to be easy to understand, and get started with. For example, it deconstructs spans into columns, so you can perform ad-hoc queries using SQL. However, this component has known performance issues: queries will eventually take seconds to return if you put a lot of data into it.


### Running in Docker

- Pull `Master` & `Cluster` images

```bash
docker pull docker.apiacmed.com/library/remoting-master:2.3.1-RC1
docker pull docker.apiacmed.com/library/remoting-server-wss:2.3.1-RC1

```

- Startup All

> 注意: Master需要映射宿主机端口: 13111 & 13110 , Cluster需要映射到宿主机端口: 23111 & 8888

> 192.168.1.151 

```bash
# 启动 Master 
docker run -p 13111:13111 -p 13110:13110 \ 
    --net docker-br0 --ip 172.172.1.155 \ 
    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \ 
    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \ 
    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \ 
    -d -v /tmp/logs/remoting-master:/remoting-master/logs \ 
    --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.1-RC1

# 启动 Cluster

docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 \ 
    --net docker-br0 --ip 172.172.1.160 \ 
    --env WSS_HOST=192.168.1.151 \ 
    --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 \ 
    --env WSS_PORT=8888 \ 
    --add-host gateway.acmedcare.com:172.172.1.108 \ 
    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \ 
    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \
    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \ 
    -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs \ 
    --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.1-RC1
```

> 192.168.1.152

```bash
# 启动Master
docker run -p 13111:13111 -p 13110:13110 \ 
    --net docker-br0 --ip 172.172.0.155 \ 
    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \ 
    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \ 
    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \ 
    -d -v /tmp/logs/remoting-master:/remoting-master/logs \ 
    --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.1-RC1

# 启动 Cluster

docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 \ 
    --net docker-br0 --ip 172.172.0.160 \ 
    --env WSS_HOST=192.168.1.151 \ 
    --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 \ 
    --env WSS_PORT=8888 \ 
    --add-host gateway.acmedcare.com:172.172.1.108 \ 
    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \ 
    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \
    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \ 
    -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs \ 
    --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.1-RC1

```


### Running the server from source

- Building

```bash
  git clone https://www.github.com/miss Elve I/Acmedcare-OpenIM.git
  cd Acmedcare-OpenIM
  mvn clean install -DskipTests=true
```

- Running

> Master server 

```bash

  cd remoting-master/target/
  unzip remoting-master-*.zip -d remoting-master-server
  cd remoting-master-server
  
  # startup
  sh bin/startup.sh -e dev -p test

```

> Cluster server 

```bash

  cd remoting-server-wss/target/
  unzip remoting-server-wss-*.zip -d remoting-server-wss
  cd remoting-server-wss
  
  # startup
  sh bin/startup.sh -e dev -p test

```


## Artifacts
> building


## Thanks

<a href="https://www.jetbrains.com/?from=Acmedcare-OpenIM">
<img src="doc/jetbrains.png" width="10%" height="10%" />
</a>
