#!/usr/bin/env bash

db_name=$1

psql $db_name -U postgres -q -S -c "
BEGIN;

alter table entry add column timestamp_long integer;
update entry e1 set timestamp_long=trunc(extract(epoch from e2.timestamp)) from entry e2 where e1.entry_number=e2.entry_number;
alter table entry drop column if exists timestamp;
alter table entry rename column timestamp_long to timestamp;

COMMIT;
"
