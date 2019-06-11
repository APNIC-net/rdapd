#!/bin/sh
echo "Launching with arguments: $@"
exec java ${JAVA_OPTS} -jar @project.artifactId@-@project.version@.@project.packaging@ "$@"
