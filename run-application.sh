#!/usr/bin/env bash
set -e

ENVIRONMENT=${ENVIRONMENT:-beta}
REGISTERS=${REGISTERS:-"country"}

if [ $ENVIRONMENT == beta ]
then
  DOMAIN="register.gov.uk"
else
  DOMAIN="$ENVIRONMENT.openregister.org"
fi

function on_exit {
  echo "Stopping and removing containers..."
  docker-compose --file docker-compose.register.yml down
  docker-compose --file docker-compose.basic.yml down
  exit
}

function wait_for_http_on_port {
  while ! curl "http://localhost:$1" --silent --fail --output /dev/null;
  do
    if [ $(docker inspect -f {{.State.Running}} $2) != 'true' ]; then
      echo "Container $2 unexpectedly stopped while waiting for it to open port $1"
      exit 1
    fi
    echo "Waiting for HTTP on :$1"
    sleep 1
  done
}

function do_nothing_forever {
  tail -f /dev/null
}

trap on_exit EXIT

git submodule update --init

if [ ! -e "./deploy/openregister-java.jar" ]
then
  docker run \
    --rm \
    --volume "$PWD":/usr/src/openregister-java \
    --workdir /usr/src/openregister-java \
    openjdk:8 \
      bash -c "./gradlew assemble"
fi

echo "Starting environment based off \"$ENVIRONMENT\""
echo "Starting basic registers..."
docker-compose --file docker-compose.basic.yml up -d
wait_for_http_on_port 8081 openregister-basic

for register in "register" "datatype" "field"; do
  echo "Loading $register..."
  curl \
    --fail \
    --header "Content-Type: application/uk-gov-rsf" \
    --header "Host: $register" \
    --data-binary @<(curl "https://$register.$DOMAIN/download-rsf") \
    --user foo:bar \
    "http://localhost:8081/load-rsf"
done

echo "Starting register..."
docker-compose --file docker-compose.register.yml up -d
wait_for_http_on_port 8080 openregister-register

for register in $REGISTERS; do
  echo "Loading $register..."
  curl \
    --fail \
    --header "Content-Type: application/uk-gov-rsf" \
    --header "Host: $register" \
    --data-binary @<(curl "https://$register.$DOMAIN/download-rsf") \
    --user foo:bar \
    "http://localhost:8080/load-rsf"
done

echo "Register is ready on http://localhost:8080"
do_nothing_forever
