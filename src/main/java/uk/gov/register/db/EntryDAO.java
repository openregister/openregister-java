package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Entry;
import uk.gov.register.store.postgres.BindEntry;

@UseStringTemplate3StatementLocator
public interface EntryDAO {
    @SqlBatch("insert into \"<schema>\".<entry_table>(entry_number, timestamp, key, type, sha256hex, blob_hash) values(:entry_number, :timestampAsLong, :key, :entryType::\"<schema>\".ENTRY_TYPE, :itemHash, :itemHash)")
    @BatchChunkSize(1000)
    void insertInBatch(@BindEntry Iterable<Entry> entries, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlUpdate("update \"<schema>\".current_entry_number set value=:entryNumber")
    void setEntryNumber(@Bind("entryNumber") int currentEntryNumber, @Define("schema") String schema);
}
