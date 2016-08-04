#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "

drop table if exists current_keys;
drop table if exists current_entry_number;
drop table if exists total_records;
drop table if exists total_entries;
drop table if exists item;
drop table if exists entry;

"
