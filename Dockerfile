FROM openjdk:8
MAINTAINER haridas <haridas.kakunje@tarento.com>
RUN mkdir -p /home/attachment
ADD target/frac-0.0.1-SNAPSHOT.jar frac-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "/frac-0.0.1-SNAPSHOT.jar"]
EXPOSE 8090