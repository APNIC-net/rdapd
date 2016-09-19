FROM openjdk:8-jre-alpine
MAINTAINER no-reply@apnic.net
ADD history-server-1.0.0-SNAPSHOT.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]