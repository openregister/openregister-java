#!/usr/bin/env bash

db_name=school

sh create_schema.sh $db_name

if [ `psql $db_name -U postgres -A -t -c "select count(*) from ordered_entry_index"` -eq 0 ]; then
    ./gradlew loadSchoolData
fi

./gradlew run
