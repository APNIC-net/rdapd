# Configuration

## Configuration File

rdapd is configured through a YAML file by the name of
*application.yml*.  The static path of the file is
```src/main/resource/application.yml```.  Its syntax is like so:

```
# Protocol-specific settings.
rdap:
  # A list of default notices that are appended to each RDAP response
  # from this server.  Notice objects take the following structure.
  # title:       # Title of the notice
  # description: # List of string describing the notice
  # links:       # List of link objects in the folloing form.
  #   - href:       # href value as per RDAP spec
  #     rel:        # ref value as per RDAP spec
  #     type:       # Type of link
  notices: []

  # Port43 value for RDAP responses.
  port43: null

# Database infomation for a database that matches the RIPE schema.
database:
  host: localhost
  database: rdapd
  username: root
  password: null

spring:
  datasource:
    url: "jdbc:mysql://${database.host}:3306/${database.database}?useunicode=true&characterencoding=utf8&charactersetresults=utf8&useSSL=false"
    username: "${database.username}"
    password: "${database.password}"

management:
  port: 8081
```

To configure rdapd at runtime, it's necessary to create a
configuration file that can be given to the application. See
the [deploy](deploy.md) documentation.
