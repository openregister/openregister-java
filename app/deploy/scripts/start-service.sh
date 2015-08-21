#!/bin/bash
INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
export AWS_DEFAULT_REGION=eu-west-1
REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Name --query 'Tags[0].Value' --output text)

aws s3 cp s3://preview.config/${REGISTER_NAME}/mint/mint.properties /srv/mint --region eu-west-1
docker run -d --name=mintApp -p 4567:4567 \
    --volume /srv/mint:/srv/mint \
    --link etc_kafka_1:kafka \
    jstepien/openjdk8 java -jar /srv/mint/mint.jar config.file=/srv/mint/mint.properties
