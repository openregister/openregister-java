#!/bin/bash
aws s3 cp s3://preview.config/mint/mint.properties /srv/mint --region eu-west-1
docker run -d --name=mintApp -p 4567:4567 \
    --volume /srv/mint:/srv/mint \
    --link etc_kafka_1:kafka \
    jstepien/openjdk8 java -jar /srv/mint/mint.jar config.file=/srv/mint/mint.properties
