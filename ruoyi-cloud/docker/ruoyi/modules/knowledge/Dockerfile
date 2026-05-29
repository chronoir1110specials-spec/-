FROM eclipse-temurin:17-jre
MAINTAINER ruoyi

RUN mkdir -p /home/ruoyi
WORKDIR /home/ruoyi
COPY ./jar/ruoyi-knowledge.jar /home/ruoyi/ruoyi-knowledge.jar
ENTRYPOINT ["java","-jar","ruoyi-knowledge.jar"]
