
FROM mysql:latest

ADD ./mysql/mysqld.cnf /etc/mysql/mysql.conf.d/mysqld.cnf

WORKDIR /docker-entrypoint-initdb.d

COPY ./sql/00-create-schema.sql ./
COPY ./sql/01-populate-schema.sql ./

