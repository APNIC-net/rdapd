
# Test SnapShot File

## Introduction
This scripts in this directory setup an enviornment for which a test snapshot
file can be exported from a running rdapd service.

## Requirements

The following is a list of requirements that need to be present on the host
system.

- Docker CE (latest version)
- Docker Compose (latest version)

## Configuration

To build the test snapshot file the Docker compose .env file needs to be edited
with the location to save the snapshot file.

Edit:

```
SNAPSHOT_DIR_PATH=/tmp/rdapd-test
```

## Running

To run the rdapd to create the test snapshot file. Use the following Docker
Compose command.

```
docker-compose up -d
```

## Generating The Snapshot File

The final step in creating the snapshot file requires issueing a HTTP GET
request to the following URL on your local system.

```
http://localhost:8081/snapshot
```
