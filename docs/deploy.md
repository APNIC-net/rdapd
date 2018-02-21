# Deploying

## Getting The Container

See the [building](building.md) documentation for instructions on
building the rdapd Docker image.

# Running The Container

```
docker run -p 8080:8080 apnic/rdapd --name rdapd
```

A custom [configuration](config.md) file may also be supplied:

```
docker run -v "<absolute_config_file_path>:/app/config/application.yml" -p 8080:8080 apnic/rdapd --name rdapd
```
