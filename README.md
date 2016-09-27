# WHOWAS query service

[![Build Status](https://travis-ci.org/APNIC-net/whowas-service.svg?branch=master)](https://travis-ci.org/APNIC-net/whowas-service)

The WHOWAS query service takes historical data imported from the RIPE database
`last` and `history` tables and produces an INR and timestamp indexed list of
RDAP objects, and exposes an API to serve RDAP-like responses.

### Quick start

#### Building

    docker build -t whowas-server .
    # And have some patience…

#### Running

    docker run --name whowas-server -d -p 80:8080 whowas-server <arguments>
    # And probably have more patience…

You may wish to mount a volume with `application.properties` to further configure
the server, or provide command line arguments with property settings.

The server will need to load some data before it can usefully start up.  Data
can be loaded directly from a RIPE database, or from a snapshot file.

The server is very memory-hungry; the docker container by default will allocate a
10G heap, and the typical runtime space requirement for the history of APNIC is
between 5G and 8G.

##### From a RIPE database

Provide an `application.properties` file or suitable arguments to configure a Spring
data source.  See the [test properties](src/main/resoures/application-test.properties)
for an example.

##### From a snapshot file

See below for how to create a snapshot file.

Provide an `application.properties` file or suitable argument to set the `snapshot.file`
property to some (volume mounted) file a snapshot may be read from.  If the snapshot
cannot be read, the server will fall back to attempting to read from a RIPE database,
and so you may use a snapshot to speed up the start time of a server, and reduce the
load on the database instance.

### Snapshots

Snapshots may be written to the location given by the `snapshot.file` property by
invoking the `snapshot` actuator endpoint, either using HTTP over the management
port (by default, this is port 8081, which is not exposed by the above `docker` command)
or using JMX (not exposed by default).

Snapshots only make sense if data has already been loaded from a RIPE database.

### Caveats

  - The server never refreshes itself, so once it's started up, it's serving a static view.
  - The RDAP output is not complete: entities aren't listed.
  - Only IPv4 is supported right now.
  - Searching for 0/0 is slower than it could be.

### How the history is constructed

For each object type, the service will create an "applicable date range" from
the time of the object's change until the time of the next object with the
same key is encountered, as a left-closed interval.

Next, for the Internet number resource object types, recursively for each
referenced additional object, the date range will be split for each date
range at which there's an interval for the referenced object.  That is, if
some `inetnum` holds between times $[a, b)$ and some `person` object it refers
to has two periods $[i, j)$ and $[j, k)$ where $a < j < b$, there will be
two separate `inetnum` records created with the two different `person` object
values assigned respectively.  This will continue until all referenced objects
have been accounted for.

Finally, these related collections of WHOIS objects will be converted to their
equivalent RDAP representations, and stored in an AVL tree.

This tree can be queried through a simple web responder.
