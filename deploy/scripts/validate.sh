#!/bin/bash

set -e

while ! curl -sf -o /dev/null http://0.0.0.0:8081/healthcheck
do
    echo "Sleeping 5 seconds and then retry..."
    sleep 5
done

echo "Service is up."
echo "Wait for instance to be added to ELB..."

# A hack to make sure the instance is back in the ELB before attempting
# to deploy to the next instance. This should only exist for as long as
# we don't have "proper" zero-downtime deploys.
ELB_INTERVAL=10
ELB_HEALTHY_THRESHOLD=4
sleep $(($ELB_INTERVAL * ($ELB_HEALTHY_THRESHOLD + 1)))

echo "Done."
exit 0;
