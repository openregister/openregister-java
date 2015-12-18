#!/bin/sh

ocd=$(pwd)
cd ..

gradle clean build assemble -x test

[[ -f build/libs/mint.jar ]] && cp build/libs/mint.jar docker/mint.jar
[[ -f build/resources/main/application.properties ]] && cp build/resources/main/application.properties docker/mint.properties

cd docker
yes | docker-compose stop
yes | docker-compose rm
docker-compose build
docker-compose up

cd $ocd
