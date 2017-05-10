FROM openjdk:8-jdk

ENV MAVEN_VERSION=3.3.9 \
    M2_HOME=/m2

RUN cd /tmp && \
    wget http://www-eu.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    tar xf apache-maven-$MAVEN_VERSION-bin.tar.gz && \
    mv apache-maven-$MAVEN_VERSION $M2_HOME && \
    rm -f apache-maven-$MAVEN_VERSION-bin.tar.gz

COPY pom.xml /build/
# Download a large number of dependencies early, such that source file changes don't require this step to re-run
RUN cd build && $M2_HOME/bin/mvn package clean --fail-never

COPY src/ build/src/
RUN cd build && \
    $M2_HOME/bin/mvn package -DskipDocker && \
    mkdir /app && \
    cp target/*.jar /app/ && \
    cp target/docker-extras/entrypoint.sh /app/ && \
    chmod 0744 /app/entrypoint.sh && \
    cd / && \
    rm -rf /build ${M2_HOME}

WORKDIR /app
RUN useradd -MrU history && \
    chown -R history /app

EXPOSE 8080
USER history
ENTRYPOINT ["./entrypoint.sh"]
