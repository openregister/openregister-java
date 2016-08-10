#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "

create table if not exists total_records(count INTEGER);
create table if not exists total_entries(count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now());
create table if not exists item (sha256hex varchar primary key, content jsonb);
create table if not exists entry (entry_number integer primary key, sha256hex varchar, timestamp INTEGER);
create table if not exists current_keys(key VARCHAR PRIMARY KEY, entry_number INTEGER UNIQUE);
create table if not exists current_entry_number(value integer not null);

insert into total_records (count) select 0 where not exists (select count from total_records);
insert into total_entries (count) select 0 where not exists (select count from total_entries);

"
