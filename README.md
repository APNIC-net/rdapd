# rdapd

[![Build Status](https://travis-ci.org/APNIC-net/rdapd.svg)](https://travis-ci.org/APNIC-net/rdapd)

rdapd takes historical data imported from the RIPE database `last` and
`history` tables and exposes an RDAP API for current state, as well as
RDAP-like responses for historical state.

# RDAP Specification

The RDAP protocol is specified in the following IETF RFCs:

- [RFC7480 - HTTP Usage in the Registration Data Access Protocol](https://tools.ietf.org/html/rfc7480)
- [RFC7481 - Security Services for the Registration Data Access Protocol](https://tools.ietf.org/html/rfc7481)
- [RFC7482 - Registration Data Access protocol (RDAP) Query Format](https://tools.ietf.org/html/rfc7482)
- [RFC7483 - JSON Responses for the Registration Data Access Protocol (RDAP)](https://tools.ietf.org/html/rfc7483)
- [RFC7484 - Finding the Authoritative Registration Data (RDAP) Service](https://tools.ietf.org/html/rfc7484)

The specification for the historical state responses is:

- [History of records in the Registration Data Access Protocol](https://tools.ietf.org/id/draft-ellacott-historical-rdap-00.html)

# Notes

- rdapd is only able to load its data from a [RIPE-compatible Whois
  database](https://github.com/RIPE-NCC/whois).

- The server is very memory-hungry; the docker container by default
  will allocate a 10G heap, and the typical runtime space requirement
  for the history of APNIC is between 5G and 8G.

- The `nameserver` endpoint is not implemented.

# Documentation

Documentation for the project can be found in the *docs/*
subdirectory:

- [Building The Project](docs/building.md)
- [Deploying](docs/deploy.md)
- [Configuration](docs/config.md)

# See Also

- [rdap-ingressd](https://github.com/APNIC-net/rdap-ingressd)
    - An RDAP server that accepts arbitrary queries and redirects
      clients to the appropriate authoritative RDAP server.

- [rdap-history-ui](https://github.com/APNIC-net/rdap-history-ui)
    - A user interface for queries for historical state.

# Licensing

rdapd is licensed under the BSD licence. See the [LICENSE file](LICENSE.txt).
