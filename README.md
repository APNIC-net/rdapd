# WHOWAS query service

[![Build Status](https://travis-ci.org/APNIC-net/whowas-service.svg?branch=master)](https://travis-ci.org/APNIC-net/whowas-service)

The WHOWAS query service takes historical data imported from the RIPE database
`last` and `history` tables and produces an INR and timestamp indexed list of
RDAP objects, and exposes an API to serve RDAP-like responses for both current
and historical state.

# Licensing
whowas is licensed under the BSD licence. Check the [LICENSE file](LICENSE.txt).

# RDAP Specification
The RDAP protocol is specificed in the following IETF RFCs:

- [RFC7480 - HTTP Usage in the Registration Data Access Protocol](https://tools.ietf.org/html/rfc7480)
- [RFC7481 - Security Services for the Registration Data Access Protocol](https://tools.ietf.org/html/rfc7481)
- [RFC7482 - Registration Data Access protocol (RDAP) Query Format](https://tools.ietf.org/html/rfc7482)
- [RFC7483 - JSON Responses for the Registration Data Access Protocol (RDAP)](https://tools.ietf.org/html/rfc7483)
- [RFC7484 - Finding the Authoritative Registration Data (RDAP) Service](https://tools.ietf.org/html/rfc7484)
- [History of records in the Registration Data Access Protocol](https://tools.ietf.org/id/draft-ellacott-historical-rdap-00.html)

# See Also
*whowas* may be used as a standlone application or in conjunction with other
side projects that provide a comprehensive system for users of the RDAP
protocol. Please see these other projects:

- [rdap-ingressd](https://github.com/APNIC-net/rdap-ingressd)
- [rdap-history-ui](https://github.com/APNIC-net/rdap-history-ui)

# Notes
- Currently whowas loads it data from a RIPE compatible database. Data can not
  be loaded from anyother source at present.

- *whowas* requires enough memory propotional to the amount of data it has to
  load. Figuring out memory requirements for a given database will require
  tweaks to the JVM memory limits till an acceptable range is found.

- Currently does not implement all RDAP current stat endpoints such as
  nameserver returning instead a not implemented response. *whowas* has enough
  extensability to add these endpoints should they be needed.

# Documentation

Documentation for the project can be found in the *docs/* subdirectory
containing information on how to configure the project.

To quickly get started, please refer to the following documentation:

- [Building The Project](docs/building.md)
- [Deploying](docs/deploy.md)
- [Configuration](docs/config.md)
