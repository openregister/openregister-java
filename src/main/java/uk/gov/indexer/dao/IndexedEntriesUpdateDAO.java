package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

interface IndexedEntriesUpdateDAO {
    String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (SERIAL_NUMBER INTEGER PRIMARY KEY, ENTRY JSONB)")
    void ensureIndexedEntriesTableExists();

    @SqlUpdate("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(SERIAL_NUMBER, ENTRY) VALUES(:serial_number, :entry)")
    int write(@Bind("serial_number") int serial_number, @Bind("entry") PGobject entry);

    @SqlQuery("SELECT MAX(SERIAL_NUMBER) FROM " + INDEXED_ENTRIES_TABLE)
    int lastReadSerialNumber();
}
