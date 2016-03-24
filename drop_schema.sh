#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "

DROP TABLE IF EXISTS current_keys;

DROP TABLE IF EXISTS total_records;

DROP TABLE IF EXISTS ordered_entry_index;

DROP TABLE IF EXISTS total_entries;

DROP TABLE IF EXISTS item;

DROP TABLE IF EXISTS entry;

DROP TABLE IF EXISTS sth; -- TODO: no longer used, should be deleted after a while

"
