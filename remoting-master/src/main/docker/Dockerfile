# Dockerfile for remoting-master
# Build with:
# docker build -t remoting-master .
# Run with:
# docker run -p 8849:8849 -d -v /tmp/logs:/opt/logs --name remoting-master remoting-master

FROM openjdk:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.2.2.BUILD-SNAPSHOT

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
