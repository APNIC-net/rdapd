FROM maven:3.5-jdk-8-alpine

ENV APP_DIR=/app
ENV BUILD_DIR=/build
ENV MAVEN_VERSION=3.3.9
ENV RESOURCE_DIR=$BUILD_DIR/target/classes

EXPOSE 8080

WORKDIR $BUILD_DIR
COPY pom.xml ./
# Download a large number of dependencies early, such that source file changes don't require this step to re-run
RUN mvn package clean --fail-never

COPY src/ ./src/
RUN mvn package -DskipDocker && \
    mkdir -p $APP_DIR/config && \
    cp target/*.jar $APP_DIR && \
    cp target/docker-extras/entrypoint.sh $APP_DIR && \
    chmod 0744 $APP_DIR/entrypoint.sh && \
    cp $RESOURCE_DIR/*.yml $APP_DIR/config && \
    rm -rf $BUILD_DIR ${M2_HOME}

WORKDIR $APP_DIR
RUN addUser -S rdapd && \
    chown -R rdapd /app

USER rdapd
ENTRYPOINT ["./entrypoint.sh"]
