#!/bin/bash
set -e

while [[ `curl -s -o /dev/null -w "%{http_code}" --resolve country.openregister.org:80:127.0.0.1 http://country.openregister.org/` != "200" ]]
do
    echo "Sleeping 5 seconds and then retry"
    sleep 5
done
exit 0;
