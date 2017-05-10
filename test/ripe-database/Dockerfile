
FROM mysql:latest

ADD ./mysql/mysqld.cnf /etc/mysql/mysql.conf.d/mysqld.cnf
ADD ./health-check /usr/local/bin/health-check

WORKDIR /docker-entrypoint-initdb.d

COPY ./sql/00-create-schema.sql ./
COPY ./sql/01-populate-schema.sql ./

HEALTHCHECK --timeout=30s \
            --interval=15s \
            --retries=4 \
            CMD health-check

