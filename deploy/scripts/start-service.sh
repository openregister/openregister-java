#!/bin/bash
INSTANCE_ID=$(curl 169.254.169.254/2014-11-05/meta-data/instance-id)
export AWS_DEFAULT_REGION=eu-west-1
REGISTER_NAME=$(aws ec2 describe-tags --filters Name=resource-id,Values=$INSTANCE_ID Name=key,Values=Name --query 'Tags[0].Value' --output text)

aws s3 cp s3://preview.config/${REGISTER_NAME}/presentation/config.yaml /srv/presentation --region eu-west-1
docker run --name=presentationApp -d -p 80:8080 --volume /srv/presentation:/srv/presentation \
    jstepien/openjdk8 java -jar /srv/presentation/presentation.jar server /srv/presentation/config.yaml
