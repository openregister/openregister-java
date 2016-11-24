#!/usr/bin/env bash

set -eu

config_file=$1

java -jar build/libs/openregister-java-all.jar db clean $config_file
