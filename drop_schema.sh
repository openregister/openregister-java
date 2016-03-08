#!/usr/bin/env bash

db_name='ft_presentation'

psql $db_name -U postgres -c "DROP TABLE IF EXISTS current_keys"
psql $db_name -U postgres -c "DROP TABLE IF EXISTS total_records"
psql $db_name -U postgres -c "DROP TABLE IF EXISTS ordered_entry_index"
psql $db_name -U postgres -c "DROP TABLE IF EXISTS total_entries"
