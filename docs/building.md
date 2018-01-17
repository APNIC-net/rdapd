# Building

WHOWAS can be built either from source, or as a Docker container.

## Requirements

- [Git](https://git-scm.com/)

### From Source

- Java 8 or higher. Supports both Oracle JDK and OpenJDK
- [Maven](https://maven.apache.org/) 3.5 or higher

### Docker Container

- [Docker CE](https://www.docker.com/community-edition) 17 or higher

## Obtaining The Source Code

```
git clone https://github.com/APNIC-net/whowas-service
```

## Building & Running From Source

### Building

WHOWAS is built using Maven. To create a new build of the project,
please run the following maven command:

```
mvn package
```

The project's JARs will now be available in the `target` directory.

### Running

The project can be executed in one of two ways: via Maven, or by
executing the JAR directly.

Executing with Maven:

```
mvn spring-boot:run
```

Executing with Java:

```
java -jar target/rdap-ingressd-<version>.jar
```

Where <version> is the version of the build.

WHOWAS is now listening and available on port 8080.

Please note that WHOWAS may require more memory than what is allowed
by default (see the JVM -Xms and -Xmx options).

## Building & Running With Docker

### Building

Use the following command to build a Docker image of WHOWAS:

```
docker build . -t apnic/whowas
```

### Running

The Docker image can now be executed with the following:

```
docker run -p 8080:8080 apnic/whowas
```

WHOWAS is now listening and available on port 8080.

See the [deploy](deploy.md) documentation for more detailed instructions on
deploying the Docker image.
