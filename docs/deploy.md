# Deploying

## Getting The Container

See the [building](building.md) documentation for instructions on
building the WHOWAS Docker image.

# Running The Container

```
docker run -p 8080:8080 apnic/whowas --name whowas
```

A custom [configuration](config.md) file may also be supplied:

```
docker run -v "<absolute_config_file_path>:/app/config/application.yml" -p 8080:8080 apnic/whowas --name whowas
```
