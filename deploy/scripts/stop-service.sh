#!/bin/bash -x

docker stop openregister
docker rm openregister

docker stop fluentd
docker rm fluentd

exit 0
