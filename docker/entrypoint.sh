#!/bin/sh

echo "Launching with arguments: $@"

JVM_INIT_MEM="${JVM_INIT_MEM:-4G}"
JVM_MAX_MEM="${JVM_MAX_MEM:-8G}"

exec java -Xms${JVM_INIT_MEM} -Xmx${JVM_MAX_MEM} \
	-jar /app/@project.artifactId@-@project.version@.@project.packaging@ \
	--spring.config.location=/app/config/application.yml
