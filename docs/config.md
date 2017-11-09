# Overview
The following documentation depicts the external configuration that is
possible in *whowas*.

# Configuration File
*whowas* is configured through a YAML file by the name of
*application.yml*.  The static path of the file is
```src/main/resource/application.yml```.  Its syntax is like so:

```
# Settings to specifically control the RDAP protocol used by this application.
rdap:

  # A list of default notices that get appended to each RDAP response from this
  # server. Notice objects take the following structure.
  # title: # Title of the notice
  # description: # List of string describing the notice
  # links: # List of link objects in the folloing form.
  #   - href: # href value as per RDAP spec
  #     rel: #ref value as per RDAP spec
  #     type: #type of link
  notices: []

  # Port43 value for RDAP responses
  port43: null

# Database infomation for a DB that matches the RIPE schema
database:
  host: localhost
  database: whowas
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

To configure *whowas* at runtime, it's necessary to create a
configuration file that can be given to the application. See
[deploy](deploy.md) documentation.
