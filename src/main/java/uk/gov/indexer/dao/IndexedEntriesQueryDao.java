package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface IndexedEntriesQueryDao {
    static final String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (ID SERIAL PRIMARY KEY, ENTRY JSONB)")
    public abstract void ensureIndexedEntriesTableExists();

    @SqlUpdate("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(ENTRY) VALUES(:entry)")
    abstract int write(@Bind("entry") PGobject entry);
}
