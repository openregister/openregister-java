#!/bin/bash

set -e

container_ip=$(docker inspect -f '{{.NetworkSettings.IPAddress}}' openregister)

while ! curl -sf -o /dev/null http://${container_ip}:8081/healthcheck
do
    echo "Sleeping 5 seconds and then retry"
    sleep 5
done
exit 0;
