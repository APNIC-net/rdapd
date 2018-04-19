#!/bin/sh

echo "Launching with arguments: $@"

exec java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
    -jar /app/rdapd.jar \
    --spring.config.location=/app/config/application.yml
