# Overview
The following document contains information for building *whowas* from both
source and as a Docker container.

# Requirements
The following requirements need to be available in order to build the project.

- [Git](https://git-scm.com/)

From Source:

- Java 8 or higher. Supports both Oracle JDK and OpenJDK
- [Maven](https://maven.apache.org/) 3.5 or higher

Docker Container:

- [Docker CE](https://www.docker.com/community-edition) 17 or higher

# Obtaining The Source Code

The first step in building *whowas* is to obtain the source code with
git.

```
git clone https://github.com/APNIC-net/whowas-service
```

# Building & Running From Source

## Building
*whowas* is built using maven. To create a new build of the project
please run the following maven command.

```
mvn package
```

The projects jars have now been created and can be executed.

## Running
The project can be executed in one of two ways. The first is through maven using
spring-boot or by executing the create jar directly with java.

Executing with maven:

```
mvn spring-boot:run
```

Executing with java:

```
java -jar target/rdap-ingressd-<version>.jar
```
Where <version> is the version of the project that has been checkout with git.

*whowas* is now listening and available on port 8080.

Please note that *whowas* may require more memory because of its data loading.
The jvm options -Xms and -Xmx can be used to adjust these values.

# Building & Running With Docker

## Building
Use the following command to build a docker image of *whowas*

```
docker build . -t apnic/whowas
```

## Running
The created docker image can now be executed with the following:

```
docker run -p 8080:8080 apnic/whowas
```

*whowas* is now listening and available on port 8080.

See the [deploy](deploy.md) documentation for more detailed instructions on
deploying the Docker image.

# Validating
To validate that *whowas* is working an RDAP query can be issued against
the service:

```
curl -X GET http://localhost:8080
```
