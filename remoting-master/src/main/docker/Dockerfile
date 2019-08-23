# Dockerfile for remoting-master
# Build with:
# docker build -t remoting-master .
# Run with:
#
# 151:
#   docker run -p 13111:13111 -p 13110:13110 --net docker-br0 --ip 172.172.1.155 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.0-RC1
#
# 152:
#   docker run -p 13111:13111 -p 13110:13110 --net docker-br0 --ip 172.172.0.155 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.0-RC1
#
#
#=========================
#
# 151:
#   docker run -p 13111:13111 -p 13110:13110 --net docker-br0 --ip 172.172.1.155 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.0-RC1
#
# 152:
#   docker run -p 13111:13111 -p 13120:13110 --net docker-br0 --ip 172.172.0.155 --add-host node1.mongodb.acmedcare.com:172.172.0.103 --add-host node2.mongodb.acmedcare.com:172.172.0.104 --add-host node3.mongodb.acmedcare.com:172.172.0.105 -d -v /tmp/logs/remoting-master:/remoting-master/logs --name remoting-master docker.apiacmed.com/library/remoting-master:2.3.0-RC1
#

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
ADD remoting-master-${VERSION}.zip /remoting-master/remoting-master-${VERSION}.zip

# add tls certs
ADD certs/tls.properties /etc/tiffany.quantum/tls.properties

ADD certs/demoCA/cacert.pem /acmedcare/applications/certs/demoCA/cacert.pem

# add tls client cert
ADD certs/client/client-key.pem /acmedcare/applications/certs/client/client-key.pem
ADD certs/client/client-cert.pem /acmedcare/applications/certs/client/client-cert.pem

# add tls server cert
ADD certs/server/server-key.pem /acmedcare/applications/certs/server/server-key.pem
ADD certs/server/server-cert.pem /acmedcare/applications/certs/server/server-cert.pem

RUN unzip /remoting-master/remoting-master-${VERSION}.zip -d /remoting-master \
    && rm -rf /remoting-master/remoting-master-${VERSION}.zip \
    && sed -i '$d' /remoting-master/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-master/bin/startup.sh

EXPOSE 13111 13110

CMD ["/remoting-master/bin/startup.sh"]
