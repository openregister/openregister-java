#!/bin/bash
aws s3 cp s3://preview.config/indexer/indexer.properties /srv/indexer --region eu-west-1
docker run -d --name=indexerApp -p 4567:4567 \
    --volume /srv/indexer:/srv/indexer \
    --link etc_kafka_1:kafka \
    jstepien/openjdk8 java -jar /srv/indexer/indexer.jar config.file=/srv/indexer/indexer.properties
