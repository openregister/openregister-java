-- Entries must always have 1 item hash
alter table entry alter column sha256hex set not null;
alter table entry_system alter column sha256hex set not null;
