#!/bin/bash

set -eu

INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
ENV=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Environment --region eu-west-1 --query 'Tags[0].Value' --output text)
CONFIG_BUCKET=openregister.${ENV}.config

aws s3 cp s3://${CONFIG_BUCKET}/indexer/indexer.properties /srv/indexer --region eu-west-1
docker run -d --name=indexerApp \
    --volume /srv/indexer:/srv/indexer \
    jstepien/openjdk8 java -jar /srv/indexer/indexer.jar config.file=/srv/indexer/indexer.properties
