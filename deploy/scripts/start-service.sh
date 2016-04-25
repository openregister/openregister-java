#!/bin/bash
set -eu

INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Name --query 'Tags[0].Value' --output text --region eu-west-1)
ENV=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Environment --query 'Tags[0].Value' --output text --region eu-west-1)
CONFIG_BUCKET=openregister.${ENV}.config

aws s3 cp s3://${CONFIG_BUCKET}/${REGISTER_NAME}/presentation/config.yaml /srv/presentation --region eu-west-1
aws s3 cp s3://${CONFIG_BUCKET}/new-registers.yaml /srv/presentation --region eu-west-1
aws s3 cp s3://${CONFIG_BUCKET}/new-fields.yaml /srv/presentation --region eu-west-1

docker run --name=presentationApp -d -p 80:8080 --volume /srv/presentation:/srv/presentation \
    jstepien/openjdk8 java -Dfile.encoding=utf-8 -DregistersYaml=/srv/presentation/new-registers.yaml -DfieldsYaml=/srv/presentation/new-fields.yaml \
    -jar /srv/presentation/presentation.jar server /srv/presentation/config.yaml
