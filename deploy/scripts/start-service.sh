#!/bin/bash
docker run --name=presentationApp -d -p 80:8080 --volume /srv/presentation:/srv/presentation \
    --link etc_kafka_1:kafka \
    --link etc_zookeeper_1:zookeeper \
    --link etc_postgres_1:postgres \
    jstepien/openjdk8 java -jar /srv/presentation/presentation.jar server /srv/presentation/config.yaml
