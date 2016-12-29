#!/bin/sh

set -eu

curl -H 'Content-type: application/uk-gov-rsf' --data-binary @school-data.rsf -u foo:bar http://localhost:8080/load-rsf