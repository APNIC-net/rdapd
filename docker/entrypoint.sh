#!/bin/sh

echo "Launching with arguments: $@"

JVM_INIT_MEM="${JVM_INIT_MEM:-4G}"
JVM_MAX_MEM="${JVM_MAX_MEM:-8G}"

set -e
echo "Starting application"
exec java -jar @project.artifactId@-@project.version@.@project.packaging@ "$@"
