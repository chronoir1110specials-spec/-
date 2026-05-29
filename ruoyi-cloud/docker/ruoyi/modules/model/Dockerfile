FROM eclipse-temurin:17-jre
MAINTAINER ruoyi

RUN mkdir -p /home/ruoyi
WORKDIR /home/ruoyi
COPY ./jar/ruoyi-model.jar /home/ruoyi/ruoyi-model.jar
ENTRYPOINT ["java","-jar","ruoyi-model.jar"]
