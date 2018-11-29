# Acmedcare+ NewIM System
---
Acmedcare NewIM System is a distributed messaging and streaming platform with low latency, high performance and reliability, trillion-level capacity and flexible scalability

It offers a variety of features:

-[x] Group & Topic message model
-[x] Pub/Sub messaging model
-[x] Message retroactivity by time or offset
-[x] Flexible distributed scale-out deployment architecture
-[x] Lightning-fast batch message exchange system
-[ ] Efficient pull&push consumption model
-[ ] Scheduled message delivery
-[ ] Log collection for streaming
-[ ] Big data integration
-[ ] Reliable FIFO and strict ordered messaging in the same queue
-[ ] Million-level message accumulation capacity in a single queue
-[x] Docker images for isolated testing and cloud isolated clusters
-[ ] Feature-rich administrative dashboard for configuration, metrics and monitoring

## RoadMap

### Master Server


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


> Endpoint Client

Development Kit for application to use master endpoint , 
like:
- Group Management
- Group Member Refs Management
- Send Message Apis



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


### Running the server from source

- Building

```bash
  git clone http://115.29.47.72:8082/acmedback/Acmedcare-NewIM.git
  cd Acmedcare-NewIM
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