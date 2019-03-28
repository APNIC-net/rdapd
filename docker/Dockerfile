FROM acr2.apnic.net/ci/alpine-jdk-maven:1.0.0

ENV APP_DIR=/app/

COPY "@project.artifactId@-@project.version@.@project.packaging@" $APP_DIR
COPY entrypoint.sh $APP_DIR
RUN chmod 0744 $APP_DIR/entrypoint.sh

WORKDIR $APP_DIR

EXPOSE 8080
EXPOSE 8081

CMD ["./entrypoint.sh"]