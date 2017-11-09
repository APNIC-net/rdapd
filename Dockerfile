FROM openjdk:8-jdk

ENV APP_DIR=/app
ENV BUILD_DIR=/build
ENV MAVEN_VERSION=3.3.9
ENV M2_HOME=/m2
ENV RESOURCE_DIR=$BUILD_DIR/target/classes

RUN cd /tmp && \
    wget http://www-eu.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar xf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    mv apache-maven-$MAVEN_VERSION $M2_HOME && \
    rm -f apache-maven-$MAVEN_VERSION-bin.tar.gz

WORKDIR $BUILD_DIR
COPY pom.xml ./
# Download a large number of dependencies early, such that source file changes don't require this step to re-run
RUN $M2_HOME/bin/mvn package clean --fail-never

COPY src/ ./src/
RUN $M2_HOME/bin/mvn package -DskipDocker && \
	mkdir -p $APP_DIR/config && \
    cp target/*.jar $APP_DIR && \
    cp target/docker-extras/entrypoint.sh $APP_DIR && \
    chmod 0744 $APP_DIR/entrypoint.sh && \
	cp $RESOURCE_DIR/*.yml $APP_DIR/config && \
    rm -rf $BUILD_DIR ${M2_HOME}

WORKDIR $APP_DIR
RUN useradd -MrU history && \
    chown -R history /app

EXPOSE 8080
USER history
ENTRYPOINT ["./entrypoint.sh"]
