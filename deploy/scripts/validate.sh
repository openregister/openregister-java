#!/bin/bash

set -e

while ! curl -sf -o /dev/null http://0.0.0.0:8081/healthcheck
do
    echo "Sleeping 5 seconds and then retry"
    sleep 5
done
exit 0;
