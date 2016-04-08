package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.*;

interface EntryDAO {
    @SqlUpdate(
            "create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp default (now() at time zone 'utc'));" +
                    "alter table entry alter column timestamp set default (now() at time zone 'utc');"
    )
    void ensureSchema();

    @SqlBatch("insert into entry(sha256hex) values(:sha256hex)")
    void insertInBatch(@Bind("sha256hex") Iterable<String> entries);
}
