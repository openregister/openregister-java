#!/bin/bash

set -eu

INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Register --region eu-west-1 --query 'Tags[0].Value' --output text)
ENV=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Environment --region eu-west-1 --query 'Tags[0].Value' --output text)
CONFIG_BUCKET=openregister.${ENV}.config
TOTAL_MEMORY=$(awk '($1 == "MemTotal:") { print $2 }' /proc/meminfo)
MAX_JVM_HEAP_SIZE=$(($TOTAL_MEMORY-512*1024))

aws s3 cp s3://${CONFIG_BUCKET}/${REGISTER_NAME}/openregister/config.yaml /srv/openregister-java --region eu-west-1
aws s3 cp s3://${CONFIG_BUCKET}/fluentd.conf /srv/openregister-java --region eu-west-1

docker run \
    --detach \
    --name=fluentd \
    --publish 24224:24224 \
    --restart "unless-stopped" \
    --volume /srv/openregister-java/fluentd.conf:/fluentd/etc/fluentd.conf \
    --env FLUENTD_CONF=fluentd.conf \
    samcrang/fluentd-sumologic

docker run \
    --detach \
    --name=openregister \
    --publish 80:8080 \
    --restart "unless-stopped" \
    --volume /srv/openregister-java:/srv/openregister-java \
    --log-driver=fluentd \
    jstepien/openjdk8 \
    java \
      -Xmx"${MAX_JVM_HEAP_SIZE}k" \
      -Dfile.encoding=UTF-8 \
      -DregistersYaml=s3://${CONFIG_BUCKET}/registers.yaml \
      -DfieldsYaml=s3://${CONFIG_BUCKET}/fields.yaml \
      -jar /srv/openregister-java/openregister-java.jar \
        server /srv/openregister-java/config.yaml
