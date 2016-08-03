#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "

CREATE TABLE IF NOT EXISTS current_keys(key VARCHAR PRIMARY KEY, entry_number INTEGER UNIQUE);

CREATE TABLE IF NOT EXISTS total_records(count INTEGER);

insert into total_records (count) select 0 where not exists (select count from total_records);

CREATE TABLE IF NOT EXISTS total_entries(count INTEGER, last_updated TIMESTAMP WITHOUT TIME ZONE DEFAULT now());

insert into total_entries (count) select 0 where not exists (select count from total_entries);

create table if not exists current_entry_number(value integer not null);

create table if not exists item (sha256hex varchar primary key, content jsonb);

create table if not exists entry (entry_number integer primary key, sha256hex varchar, timestamp timestamp without time zone default now());

"
