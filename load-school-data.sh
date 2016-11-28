#!/bin/sh

set -eu

curl -H 'Content-type: application/uk-gov-rsf' --data-binary @school-data.rsf http://localhost:8080/load-rsf

