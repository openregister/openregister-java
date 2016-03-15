package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.*;
import uk.gov.mint.DataReplicationTask;

import java.util.List;

interface EntryDAO {
    @SqlUpdate("create table if not exists entry (entry_number serial primary key, sha256hex varchar, timestamp timestamp default now());")
    void ensureSchema();

    @SqlQuery("select max(entry_number) from entry")
    int maxID();

    @SqlBatch("insert into entry(sha256hex) values(:sha256hex)")
    void insertInBatch(@Bind("sha256hex") List<String> entries);

    //Todo: methods below are temporary for migration purpose, must delete after migration
    @SqlBatch("insert into entry(entry_number, sha256hex) values(:id, :sha256hex)")
    void insertMigratedEntries(@BindBean List<DataReplicationTask.MigratedEntry> migratedEntries);

    @SqlUpdate("select setval('entry_entry_number_sequence', :lastIdMigrated)")
    void updateSequenceNumber(@Bind("lastIdMigrated") int lastIdMigrated);
}
