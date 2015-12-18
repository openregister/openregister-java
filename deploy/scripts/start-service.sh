#!/bin/bash
set -eu

INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)

REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Name --region eu-west-1 --query 'Tags[0].Value' --output text)
ENV=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Environment --region eu-west-1 --query 'Tags[0].Value' --output text)
CONFIG_BUCKET=openregister.${ENV}.config

aws s3 cp s3://${CONFIG_BUCKET}/${REGISTER_NAME}/mint/mint-config.yaml /srv/mint --region eu-west-1

aws s3 cp s3://${CONFIG_BUCKET}/registers.yaml /srv/mint --region eu-west-1
aws s3 cp s3://${CONFIG_BUCKET}/fields.yaml /srv/mint --region eu-west-1

docker run -d --name=mintApp -p 4567:4567 \
    --volume /srv/mint:/srv/mint \
    jstepien/openjdk8 java -Dfile.encoding=UTF-8 -DregistersYaml=/srv/mint/registers.yaml -DfieldsYaml=/srv/mint/fields.yaml -jar /srv/mint/mint.jar server /srv/mint/mint-config.yaml
