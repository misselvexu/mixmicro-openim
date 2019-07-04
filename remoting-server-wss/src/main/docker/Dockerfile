# Dockerfile for remoting-server-wss
# Build with:
# docker build -t remoting-server-wss .
# Run with:
# docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 --net docker-br0 --ip 172.172.0.160 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.2.3.BUILD-SNAPSHOT
#
#
#
# --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.0.156:13111
#
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

ADD remoting-server-wss-${VERSION}.zip /remoting-server-wss/remoting-server-wss-${VERSION}.zip

RUN unzip /remoting-server-wss/remoting-server-wss-${VERSION}.zip -d /remoting-server-wss \
    && rm -rf /remoting-server-wss/remoting-server-wss-${VERSION}.zip \
    && sed -i '$d' /remoting-server-wss/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-server-wss/bin/startup.sh

EXPOSE 8888 23111 33111 43111

CMD ["/remoting-server-wss/bin/startup.sh"]
