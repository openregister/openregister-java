create index if not exists entry_item_entry_number_index on entry_item (entry_number);
create index if not exists entry_item_sha256hex_index on entry_item (sha256hex);