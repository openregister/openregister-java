#!/usr/bin/env bash

db_name=openregister_java

sh create_schema.sh config.yaml

if [ `psql $db_name -U postgres -A -t -c "select count(*) from item"` -eq 0 ]; then
    ./gradlew loadSchoolData
fi

./gradlew run
