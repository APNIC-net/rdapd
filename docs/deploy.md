# Overview
Describes the process for deploying *whowas* as a Docker container.

# Getting The Container
See the [building](building.md) documentation for instructions on building the
*whowas* Docker image.

# Running The Container
The Docker container can be run standalone or by supplying a
[config](config.md) file.

```
docker run -p 8080:8080 apnic/whowas --name whowas
```

To supply a custom configuration file:

```
docker run -v "<absolute_config_file_path>:/app/config/application.yml" -p 8080:8080 apnic/whowas --name whowas
```
