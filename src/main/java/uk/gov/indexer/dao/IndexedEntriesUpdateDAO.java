package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface IndexedEntriesUpdateDAO {
    String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (SERIAL_NUMBER SERIAL PRIMARY KEY, ENTRY JSONB)")
    void ensureIndexedEntriesTableExists();

    @SqlUpdate("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(ENTRY) VALUES(:entry)")
    int write(@Bind("entry") PGobject entry);
}
