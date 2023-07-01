# Dockerfile for remoting-server-wss
# Build with:
# docker build -t remoting-server-wss .
# Run with:
#
# 151:
#   docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 --net docker-br0 --ip 172.172.1.160 --env WSS_HOST=192.168.1.151 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --env WSS_PORT=8888 --add-host gateway.acmedcare.com:172.172.1.108 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
#
# 152:
#   docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 --net docker-br0 --ip 172.172.0.160 --env WSS_HOST=192.168.1.152 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --env WSS_PORT=8888 --add-host gateway.acmedcare.com:172.172.1.108 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
#
#
#===================
#
# 151:
#   docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 --net docker-br0 --ip 172.172.1.160 --env WSS_HOST=192.168.1.151 --env EXPORT_HOST=192.168.1.166 --env IM_PORT=23111 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --env NEWIM_DELIVERER_ADDR=172.172.1.166:14110 --env WSS_PORT=8888 --add-host gateway.acmedcare.com:172.172.1.108 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
#
# 152:
#   docker run -p 43111:43111 -p 23121:23121 -p 33111:33111 -p 8888:8888 --net docker-br0 --ip 172.172.0.160 --env WSS_HOST=192.168.1.152 --env EXPORT_HOST=192.168.1.166 --env IM_PORT=23121 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --env NEWIM_DELIVERER_ADDR=172.172.1.166:14110 --env WSS_PORT=8888 --add-host gateway.acmedcare.com:172.172.1.108 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT
#

#docker run -p 43111:43111 -p 23111:23111 -p 33111:33111 -p 8888:8888 \
#    --net docker-br0 --ip 172.172.1.160 \
#    --env WSS_HOST=192.168.1.151 \
#    --env EXPORT_HOST=192.168.1.166 \
#    --env IM_PORT=23111 \
#    --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 \
#    --env NEWIM_DELIVERER_ADDR=172.172.1.166:14110 \
#    --env WSS_PORT=8888 \
#    --add-host gateway.acmedcare.com:172.172.1.108 \
#    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \
#    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \
#    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \
#    -d -v /tmp/logs/remoting-server-wss:/remoting-server-wss/logs \
#    --name remoting-server-wss docker.apiacmed.com/library/remoting-server-wss:2.3.2-BUILD.SNAPSHOT


FROM docker.apiacmed.com/env/openjdk-acmed:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.3.2-BUILD.SNAPSHOT

RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD remoting-server-wss-${VERSION}.zip /remoting-server-wss/remoting-server-wss-${VERSION}.zip

# add tls certs
ADD certs/tls.properties /etc/tiffany.quantum/tls.properties

ADD certs/demoCA/cacert.pem /acmedcare/applications/certs/demoCA/cacert.pem

# add tls client cert
ADD certs/client/client-key.pem /acmedcare/applications/certs/client/client-key.pem
ADD certs/client/client-cert.pem /acmedcare/applications/certs/client/client-cert.pem

# add tls server cert
ADD certs/server/server-key.pem /acmedcare/applications/certs/server/server-key.pem
ADD certs/server/server-cert.pem /acmedcare/applications/certs/server/server-cert.pem

RUN unzip /remoting-server-wss/remoting-server-wss-${VERSION}.zip -d /remoting-server-wss \
    && rm -rf /remoting-server-wss/remoting-server-wss-${VERSION}.zip \
    && sed -i '$d' /remoting-server-wss/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-server-wss/bin/startup.sh

EXPOSE 8888 23111 33111 43111

CMD ["/remoting-server-wss/bin/startup.sh"]
