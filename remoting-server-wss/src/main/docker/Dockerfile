# Dockerfile for remoting-server-wss
# Build with:
# docker build -t remoting-server-wss .
# Run with:
# docker run -p 8849:8849 -d -v /tmp/logs:/opt/logs --name remoting-server-wss remoting-server-wss

FROM openjdk:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.2.0-RC2

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
