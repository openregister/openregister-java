#!/bin/bash
docker run -d --name=mintApp -p 4567:4567 \
    --volume /srv/mint:/srv/mint \
    --link etc_postgres_1:postgres \
    --link etc_kafka_1:kafka \
    jstepien/openjdk8 java -jar /srv/mint/mint.jar
