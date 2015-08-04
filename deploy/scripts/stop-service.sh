#!/bin/bash -x
RES=`ps aux | grep -v grep | grep 'presentation.jar' | awk '{print $2}'`
if [[ ! -z "$RES" ]]; then
    kill $RES
fi