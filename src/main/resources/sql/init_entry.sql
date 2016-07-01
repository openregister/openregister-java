group DAO;

ensureEntrySchemaInPlace() ::= <<

create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp);

alter table entry alter column entry_number drop default;

create index if not exists entry_sha256hex_index on entry (sha256hex);

create table if not exists total_entries (count integer);

--Insert query below initializes the total_entries table by 0 if it is not initialized yet
insert into total_entries (count) select 0 where not exists (select 1 from total_entries);

>>
