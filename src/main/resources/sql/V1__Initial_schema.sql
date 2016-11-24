CREATE TABLE IF NOT EXISTS item (sha256hex VARCHAR PRIMARY KEY, content JSONB);
CREATE INDEX IF NOT EXISTS item_content_gin ON item USING gin(content jsonb_path_ops);

create table if not exists entry (entry_number integer primary key, sha256hex varchar, timestamp integer);
create index if not exists entry_sha256hex_index on entry (sha256hex);
create table if not exists current_entry_number(value integer not null);

insert into current_entry_number(value)
    select (
        select case
            when (select max(entry_number) from entry) is null then 0
            else (select max(entry_number) from entry)
        end as t
    )
    where not exists (
        select 1 from current_entry_number
    );

CREATE TABLE IF NOT EXISTS  current_keys  (key VARCHAR PRIMARY KEY, entry_number INTEGER UNIQUE);

CREATE TABLE IF NOT EXISTS  total_records  (COUNT INTEGER);

--Insert query below initializes the total records for pre existing register by setting the value as no of rows in current_keys table
INSERT INTO  total_records (COUNT) SELECT (SELECT COUNT(*) FROM  current_keys) WHERE NOT EXISTS(SELECT 1 FROM  total_records );
