create table if not exists entry_item
(
    entry_number    integer,
    sha256hex       varchar
);

insert into     entry_item (entry_number, sha256hex)
select          entry_number,
                sha256hex
from            entry;
