#!/usr/bin/env bash

set -eu

base_uri=$1
user_pass=$2

curl -f -s -u $user_pass -XDELETE $base_uri/delete-register-data
