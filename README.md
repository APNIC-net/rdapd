# WHOWAS query service

[![Build Status](https://travis-ci.org/APNIC-net/whowas-service.svg?branch=master)](https://travis-ci.org/APNIC-net/whowas-service)

The WHOWAS query service takes historical data imported from the RIPE database
`last` and `history` tables and produces an INR and timestamp indexed list of
RDAP objects.

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
