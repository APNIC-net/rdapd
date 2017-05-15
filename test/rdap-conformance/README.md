
# RDAP Conformance Testing

## Introduction

Directory contains files necessary for running the rdap-conformance program
against this project.

Please see [rdap-conformance](https://github.com/APNIC-net/rdap-conformance)
for more detail.

## Running Conformance Tests

Conformance tests can be run from the root of the project with:

```
mvn verify
```

<aside class="notice">
It's important to note that current failures in rdap-conformance will not cause
this projects maven build to fail. This is a limitation in the Docker plugin
and will be fixed in coming months.
</aside>

