package uk.gov.register.db;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.OverrideStatementRewriterWith;
import uk.gov.register.core.Entry;
import uk.gov.register.store.postgres.BindEntry;

@OverrideStatementRewriterWith(SubstituteSchemaRewriter.class)
public interface EntryDAO {
    @SqlBatch("insert into :schema.entry(entry_number, sha256hex, timestamp, key, type) values(:entry_number, :sha256hex, :timestampAsLong, :key, :entryType:::schema.ENTRY_TYPE)")
    @BatchChunkSize(1000)
    void insertInBatch(@BindEntry Iterable<Entry> entries, @Bind("schema") String schema);

    @SqlQuery("select value from :schema.current_entry_number")
    int currentEntryNumber(@Bind("schema") String schema);

    @SqlUpdate("update :schema.current_entry_number set value=:entryNumber")
    void setEntryNumber(@Bind("entryNumber") int currentEntryNumber, @Bind("schema") String schema);
}
