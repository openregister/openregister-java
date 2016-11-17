#!/usr/bin/env bash

db_name=$1
key_field=$2

if [ -z "${db_name}" ] || [ -z "${key_field}" ]; then
    echo "DB name and item key field must provided"
    echo "usage: add-item-primary-key-to-entry.sh <dbname> <item_key>"
else
    psql $db_name -U postgres -c "\set item_key '''$key_field'''" -f add-item-primary-key-to-entry.sql
fi