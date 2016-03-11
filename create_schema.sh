#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "

CREATE TABLE IF NOT EXISTS current_keys(key VARCHAR PRIMARY KEY, serial_number INTEGER UNIQUE);

CREATE TABLE IF NOT EXISTS total_records(count INTEGER);

insert into total_records (count) select 0 where not exists (select count from total_records);

CREATE TABLE IF NOT EXISTS ordered_entry_index(serial_number INTEGER PRIMARY KEY, entry JSONB);

ALTER TABLE ordered_entry_index drop column if exists leaf_input;

CREATE TABLE IF NOT EXISTS total_entries(count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now());

insert into total_entries (count) select 0 where not exists (select count from total_entries);

"
