#!/bin/bash
aws s3 cp s3://preview.config/country/presentation/config.yaml /srv/presentation --region eu-west-1
docker run --name=presentationApp -d -p 80:8080 --volume /srv/presentation:/srv/presentation \
    jstepien/openjdk8 java -jar /srv/presentation/presentation.jar server /srv/presentation/config.yaml
