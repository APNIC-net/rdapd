# Building container
FROM maven:3.5-jdk-8-alpine as builder

ENV BUILD_DIR=/build
ENV MAVEN_VERSION=3.3.9
ENV RESOURCE_DIR=$BUILD_DIR/target/classes

WORKDIR $BUILD_DIR
COPY pom.xml ./
# Download a large number of dependencies early, such that source file changes don't require this step to re-run
RUN mvn package clean --fail-never
COPY src/ ./src/
RUN mvn package -DskipDocker

FROM openjdk:8-alpine

ENV APP_DIR=/app

EXPOSE 8080
EXPOSE 8081

# Application container
RUN mkdir -p $APP_DIR/config as app
WORKDIR $APP_DIR
COPY --from=builder /build/src/main/docker/entrypoint.sh entrypoint.sh
COPY --from=builder /build/target/*jar rdapd.jar
COPY --from=builder /build/target/classes/*.yml config

RUN chmod 0744 entrypoint.sh && \
	adduser -S rdapd && \
	chown -R rdapd /app

USER rdapd
ENTRYPOINT ["./entrypoint.sh"]
