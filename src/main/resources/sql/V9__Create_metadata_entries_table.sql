CREATE TABLE IF NOT EXISTS entry_system (entry_number INTEGER PRIMARY KEY, timestamp INTEGER NOT NULL, key VARCHAR not null, type ENTRY_TYPE NOT NULL);
create table if not exists entry_item_system
(
    entry_number    integer not null,
    sha256hex       varchar not null
);
