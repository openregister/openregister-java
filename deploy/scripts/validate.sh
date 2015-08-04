#!/bin/bash
set -e
RES=`curl -s -o /dev/null -w "%{http_code}" http://localhost/`
if [[ "$RES" != "200" ]]; then
    echo "Error: can not curl the application"
    exit 255;
fi
