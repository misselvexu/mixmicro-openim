# Dockerfile for remoting-server-runner
# Build with:
# docker build -t remoting-server-runner .
# Run with:
# docker run -p 8849:8849 -d -v /tmp/logs:/opt/logs --name remoting-server-runner remoting-server-runner

FROM openjdk:8-jre-alpine
MAINTAINER Elve.Xu <iskp.me@gmail.com>

ENV VERSION 2.3.2-BUILD.SNAPSHOT

RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache procps unzip curl bash tzdata \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

ADD remoting-server-runner-${VERSION}.zip /remoting-server-runner/remoting-server-runner-${VERSION}.zip

RUN unzip /remoting-server-runner/remoting-server-runner-${VERSION}.zip -d /remoting-server-runner \
    && rm -rf /remoting-server-runner/remoting-server-runner-${VERSION}.zip \
    && sed -i '$d' /remoting-server-runner/bin/startup.sh \
    && echo "tail -f /dev/null" >> /remoting-server-runner/bin/startup.sh

EXPOSE 16677 16688 6677 6688

CMD ["/remoting-server-runner/bin/startup.sh"]
