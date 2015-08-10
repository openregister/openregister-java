#!/bin/bash
set -e

while [[ `curl -s -o /dev/null -w "%{http_code}" http://localhost/` != "200" ]]
do
    echo "Sleeping 5 seconds and then retry"
    sleep 5
done
exit 0;
