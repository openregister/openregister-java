group EntryDAO;

ensureSchema() ::= <<
   create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp default (now() at time zone 'utc'));

   alter table entry alter column timestamp set default (now() at time zone 'utc');

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

   alter table entry alter column entry_number drop default;
>>
