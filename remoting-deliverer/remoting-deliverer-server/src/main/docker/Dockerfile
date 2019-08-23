# Dockerfile for remoting-deliverer-server
# Build with:
# docker build -t remoting-deliverer-server .
# Run with:
#
#=========================
#
# 151:
#   docker run -p 14110:14110 --net docker-br0 --ip 172.172.1.166 --env DELIVERER_HOST=172.172.1.166 --env DELIVERER_PORT=14110 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-deliverer-server:/remoting-deliverer-server/logs --name remoting-deliverer-server docker.apiacmed.com/library/remoting-deliverer-server:2.3.0-RC1
#
# 152:
#   docker run -p 14110:14110 --net docker-br0 --ip 172.172.0.166 --env DELIVERER_HOST=172.172.0.166 --env DELIVERER_PORT=14110 --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-deliverer-server:/remoting-deliverer-server/logs --name remoting-deliverer-server docker.apiacmed.com/library/remoting-deliverer-server:2.3.0-RC1
#

#docker run -p 14110:14110 \
#    --net docker-br0 --ip 172.172.1.166 \
#    --env DELIVERER_HOST=172.172.1.166 \
#    --env DELIVERER_PORT=14110 \
#    --env NEWIM_MASTER_ADDR=172.172.0.155:13111,172.172.1.155:13111 \
#    --add-host node1.mongodb.acmedcare.com:172.172.0.103 \
#    --add-host node2.mongodb.acmedcare.com:172.172.0.104 \
#    --add-host node3.mongodb.acmedcare.com:172.172.0.105 \
#    -d -v /tmp/logs/remoting-deliverer-server:/remoting-deliverer-server/logs \
#    --name remoting-deliverer-server docker.apiacmed.com/library/remoting-deliverer-server:2.3.0-RC1

FROM docker.apiacmed.com/env/openjdk-acmed:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.3.0-RC1

RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# add runting package
ADD remoting-deliverer-server-${VERSION}.zip /remoting-deliverer-server/remoting-deliverer-server-${VERSION}.zip

# add tls certs
ADD certs/tls.properties /etc/tiffany.quantum/tls.properties

ADD certs/demoCA/cacert.pem /acmedcare/applications/certs/demoCA/cacert.pem

# add tls client cert
ADD certs/client/client-key.pem /acmedcare/applications/certs/client/client-key.pem
ADD certs/client/client-cert.pem /acmedcare/applications/certs/client/client-cert.pem

# add tls server cert
ADD certs/server/server-key.pem /acmedcare/applications/certs/server/server-key.pem
ADD certs/server/server-cert.pem /acmedcare/applications/certs/server/server-cert.pem

RUN unzip /remoting-deliverer-server/remoting-deliverer-server-${VERSION}.zip -d /remoting-deliverer-server \
    && rm -rf /remoting-deliverer-server/remoting-deliverer-server-${VERSION}.zip \
    && sed -i '$d' /remoting-deliverer-server/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-deliverer-server/bin/startup.sh

EXPOSE 14110

CMD ["/remoting-deliverer-server/bin/startup.sh"]
