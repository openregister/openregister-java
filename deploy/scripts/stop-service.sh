#!/bin/bash -x

docker stop openregister
docker rm openregister

docker stop fluentd
docker rm fluentd

docker stop telegraf
docker rm telegraf

docker network rm openregisters

exit 0
