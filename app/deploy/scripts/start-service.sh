#!/bin/bash
set -eu
INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
export AWS_DEFAULT_REGION=eu-west-1
REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Name --query 'Tags[0].Value' --output text)
ENV=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Environment --query 'Tags[0].Value' --output text)
CONFIG_BUCKET=openregister.${ENV}.config

aws s3 cp s3://${CONFIG_BUCKET}/${REGISTER_NAME}/mint/mint-config.yaml /srv/mint --region eu-west-1
docker run -d --name=mintApp -p 4567:4567 \
    --volume /srv/mint:/srv/mint \
    jstepien/openjdk8 java -jar /srv/mint/mint.jar server /srv/mint/mint-config.yaml
