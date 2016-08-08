group EntryDAO;

ensureSchema() ::= <<
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
>>
