# Dockerfile for remoting-master
# Build with:
# docker build -t remoting-master .
# Run with:
# docker run -p 13111:13111 -p 13110:13110 --net docker-br0 --ip 172.172.1.155 --add-host nacos.acmedcare.com:172.172.1.3 --add-host mysql.acmedcare.com:172.172.1.101 --add-host trace.acmedcare.com:192.168.1.151 --add-host redis.acmedcare.com:172.172.1.101 -d -v /tmp/logs:/tmp/logs -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.2.3.BUILD-SNAPSHOT
#
#
# docker run -p 13111:13111 -p 13110:13110 --net docker-br0 --ip 172.172.0.155 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.2.3.BUILD-SNAPSHOT
# docker run -p 23111:13111 -p 23110:13110 --net docker-br0 --ip 172.172.0.156 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master-2:/remoting-master/logs --name remoting-master-2 docker.apiacmed.com/library/remoting-master:2.2.3.BUILD-SNAPSHOT
#

FROM docker.apiacmed.com/env/openjdk-acmed:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.2.3.BUILD-SNAPSHOT

RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD remoting-master-${VERSION}.zip /remoting-master/remoting-master-${VERSION}.zip

RUN unzip /remoting-master/remoting-master-${VERSION}.zip -d /remoting-master \
    && rm -rf /remoting-master/remoting-master-${VERSION}.zip \
    && sed -i '$d' /remoting-master/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-master/bin/startup.sh

EXPOSE 13111 13110

CMD ["/remoting-master/bin/startup.sh"]
