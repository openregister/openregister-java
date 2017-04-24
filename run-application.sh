#!/usr/bin/env bash

if [ ! -e "./deploy/openregister-java.jar" ]
then
  docker run \
    --rm \
    --volume "$PWD":/usr/src/openregister-java \
    --workdir /usr/src/openregister-java \
    openjdk:8 \
      bash -c "./gradlew assemble"
fi

docker-compose up
