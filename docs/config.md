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

  # Enables history endpoints (https://github.com/APNIC-net/rdap-history).
  # If this setting is omitted, history endpoints will be active.
  historyEndpointsEnabled: true

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
    
# Configuration for loading data from RPSL dump files.
# Notice that you need to activate the "rpsl-data" spring profile to enable loading from RPSL data.
rpslData:
  # FTP URI for the RPSL dump file.
  # E.g.: "ftp://user:password@ftp.apnic.net/pub/incoming/krnic/krnic.db.tar.gz"
  uri: "ftp://template:password@ftp.apnic.net/pub/incoming/krnic/krnic.db.tar.gz"
  # Cron expression for loading RPSL data (Spring format - i.e. includes seconds).
  updateCronExpr: 0 0 * * * *

management:
  port: 8081
```

To configure rdapd at runtime, it's necessary to create a
configuration file that can be given to the application. See
the [deploy](deploy.md) documentation.
