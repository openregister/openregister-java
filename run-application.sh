#!/usr/bin/env bash

db_name=school

createdb "$db_name"

psql $db_name -U postgres -c "CREATE TABLE IF NOT EXISTS current_keys(key VARCHAR PRIMARY KEY, serial_number INTEGER UNIQUE)"
psql $db_name -U postgres -c "CREATE TABLE IF NOT EXISTS total_records(count INTEGER)"

psql $db_name -U postgres -c "insert into total_records (count) select 0 where not exists (select count from total_records)"

psql $db_name -U postgres -c "CREATE TABLE IF NOT EXISTS ordered_entry_index(serial_number INTEGER PRIMARY KEY, entry JSONB)"
psql $db_name -U postgres -c "ALTER TABLE ordered_entry_index drop column leaf_input"

psql $db_name -U postgres -c "CREATE TABLE IF NOT EXISTS total_entries(count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now())"
psql $db_name -U postgres -c "CREATE TABLE sth (tree_size INTEGER, timestamp BIGINT, tree_head_signature VARCHAR, sha256_root_hash VARCHAR)"

psql $db_name -U postgres -c "insert into total_entries (count) select 0 where not exists (select count from total_entries)"


if [ `psql $db_name -U postgres -A -t -c "select count(*) from ordered_entry_index"` -eq 0 ]; then
    ./gradlew loadSchoolData
fi

./gradlew run
