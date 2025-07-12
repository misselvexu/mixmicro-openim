# 🚀 Mixmicro OpenIM

<p align="center">
  <img src="https://img.shields.io/badge/version-2.3.2-blue.svg" alt="Version">
  <img src="https://img.shields.io/badge/language-Java-orange.svg" alt="Language">
  <img src="https://img.shields.io/badge/license-Apache%202-green.svg" alt="License">
  <img src="https://img.shields.io/badge/platform-Docker-blue.svg" alt="Platform">
</p>

---

## 📖 Overview

Mixmicro+ OpenIM is a **distributed messaging and streaming platform** designed for modern applications that demand low latency, high performance, and uncompromising reliability. Built to handle trillion-level capacity with flexible scalability, it provides enterprise-grade messaging solutions for real-time communication systems.

## ✨ Key Features

### 🔥 Core Messaging Capabilities
- ✅ **Group & Topic Message Model** - Flexible message organization and routing
- ✅ **Pub/Sub Messaging** - Scalable publish-subscribe pattern implementation  
- ✅ **Message Retroactivity** - Historical message retrieval by time or offset
- ✅ **FIFO & Ordered Messaging** - Reliable message ordering within queues
- ✅ **Scheduled Message Delivery** - Time-based message scheduling

### 🏗️ Architecture & Scalability
- ✅ **Distributed Scale-out Architecture** - Horizontal scaling capabilities
- ✅ **Lightning-fast Batch Processing** - High-throughput message exchange
- ✅ **Pull & Push Consumption Models** - Flexible message consumption patterns
- ✅ **Million-level Queue Capacity** - Single queue message accumulation

### 🔧 Integration & Operations  
- ✅ **Docker Support** - Containerized deployment for testing and production
- ✅ **Log Collection & Streaming** - Built-in logging infrastructure
- ✅ **Big Data Integration** - Seamless data pipeline connectivity
- ✅ **Administrative Dashboard** - Rich UI for configuration, metrics, and monitoring

## 🗺️ Roadmap

### 🎯 Master Server
**Naming Server** with client load-balancing, service discovery, and endpoint management

### 🔗 Cluster Server

**Protocol Support**
- ✅ TCP Protocol
- ✅ R-UDP Protocol  
- ✅ WebSocket Protocol

**Development Kits**
- ✅ Java SDK
- ✅ Android SDK
- 🔄 Node.js SDK *(In Progress)*

### 📡 Push Server

**Protocol Support**
- ✅ R-UDP Protocol

**Development Kits**
- ✅ Java SDK
- ✅ Android SDK

### 📬 Message Queue Server
Enterprise-grade message queue supporting topics, queues, and advanced routing

### 💻 Endpoint Client SDK
Comprehensive development kit providing:
- Group Management APIs
- Member Reference Management
- Message Sending APIs
- Media Message APIs

## 🚀 Quick Start

### 📋 Prerequisites
- Docker 20.0+
- MongoDB 4.0+
- Java 8+
- Maven 3.6+

### 💾 Storage Components

#### 🍃 MongoDB Setup

MongoDB serves as our primary document storage database.

> ⚠️ **Warning**: MongoDB 4.x Single Cluster does not support transactions.

**Docker Deployment**
```bash
# Pull MongoDB image
docker pull mongo

# Start MongoDB container
docker run -p 27017:27017 \
  -v /mixmicro/data/mongo:/data/db \
  --name docker_mongodb \
  -d mongo
```

**Replica Set Cluster**
```bash
# Create data directories
mkdir /mixmicro/replica-datas/cluster{1..3} -pv
```

📚 For detailed MongoDB configuration, see [MongoDB Development Documentation](mongo-configs/README.md)

### 🐳 Docker Deployment

**Pull Required Images**
```bash
docker pull docker.apiacmed.com/library/remoting-master:2.3.2-BUILD.SNAPSHOT
docker pull docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
```

**Production Deployment**

> 📝 **Note**: Master requires ports 13111 & 13110, Cluster requires ports 23111 & 8888

**Node 192.168.1.151**
```bash
# Start Master Server
docker run -p 13111:13111 -p 13110:13110 \
  --net docker-br0 --ip 172.172.1.155 \
  --add-host node1.mongodb.mixmicro.com:172.172.0.103 \
  --add-host node2.mongodb.mixmicro.com:172.172.0.104 \
  --add-host node3.mongodb.mixmicro.com:172.172.0.105 \
  -d -v /tmp/logs/remoting-master:/remoting-master/logs \
  --name remoting-master \
  docker.apiacmed.com/library/remoting-master:2.3.2-BUILD.SNAPSHOT

# Start Cluster Server
docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 \
  --net docker-br0 --ip 172.172.1.160 \
  --env WSS_HOST=192.168.1.151 \
  --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 \
  --env WSS_PORT=8888 \
  --add-host gateway.mixmicro.com:172.172.1.108 \
  --add-host node1.mongodb.mixmicro.com:172.172.0.103 \
  --add-host node2.mongodb.mixmicro.com:172.172.0.104 \
  --add-host node3.mongodb.mixmicro.com:172.172.0.105 \
  -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs \
  --name remoting-server-wss \
  docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
```

### 🛠️ Build from Source

**Clone and Build**
```bash
git clone https://github.com/missElve/Mixmicro-OpenIM.git
cd Mixmicro-OpenIM
mvn clean install -DskipTests=true
```

**Start Master Server**
```bash
cd remoting-master/target/
unzip remoting-master-*.zip -d remoting-master-server
cd remoting-master-server

# Startup with development profile
sh bin/startup.sh -e dev -p test
```

**Start Cluster Server**
```bash
cd remoting-server-wss/target/
unzip remoting-server-wss-*.zip -d remoting-server-wss
cd remoting-server-wss

# Startup with development profile  
sh bin/startup.sh -e dev -p test
```

## 📈 Performance Metrics

- **Latency**: Sub-millisecond message delivery
- **Throughput**: Million+ messages per second
- **Capacity**: Trillion-level message storage
- **Availability**: 99.99% uptime SLA

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

<p align="center">
  <a href="https://www.jetbrains.com/?from=Mixmicro-OpenIM">
    <img src="doc/jetbrains.png" width="150" alt="JetBrains">
  </a>
</p>

<p align="center">
  <strong>Special thanks to JetBrains for providing development tools</strong>
</p>

---

<p align="center">
  Made with ❤️ by the Mixmicro Team
</p>