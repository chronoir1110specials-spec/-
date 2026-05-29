FROM eclipse-temurin:17-jre
MAINTAINER ruoyi

RUN mkdir -p /home/ruoyi
WORKDIR /home/ruoyi
COPY ./jar/ruoyi-agent.jar /home/ruoyi/ruoyi-agent.jar
ENTRYPOINT ["java","-jar","ruoyi-agent.jar"]
