
# Test Ripe Database

## Introduction
This scripts in this directory are used to construct a mock RIPE database with
test data that can further be used to test the rdapd service.

<aside class="notice">
Note that the database created only contains the needed subset of tables for
the rdapd service to function.

All test data is fake and does not represent any real reasource allocations.
</aside>

## Requirements

The following is a list of requirements that need to be present on the host
system.

- Docker CE (latest version)

## Building the Docker image

To build the Docker image the following command can be issued. After running
the command a new Docker image called rdapd-ripe-db.

```
docker build . -t rdapd-ripe-db
```

Alternatively the image can be built with maven:

```
mvn -Ddocker.filter="apnic/rdapd-test-db" docker:build
```

## Running the created Docker image

After the Docker image has been created the following commmand can be issued
to run a copy of the database.

```
docker run -d -e "MYSQL_ALLOW_EMPTY_PASSWORD=yes" -p 3306:3306 --name rdapd-ripe-db rdapd-ripe-db
```

Alternatively the Docker image can be run with maven:

```
mvn -Ddocker.filter="apnic/rdapd-test-db" docker:start
```

<aside class="notice">
Please note that there is no password security on the resulting Docker
container. See the following [documentation](https://hub.docker.com/_/mysql/)
for instructions on securing the container.
</aside>

## Connecting to the container

To connect to the container from the mysql cli client the following command
can be issued.

```
mysql -u root -h 127.0.0.1
```
