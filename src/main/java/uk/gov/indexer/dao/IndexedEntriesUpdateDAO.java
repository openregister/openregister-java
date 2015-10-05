package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.util.Iterator;

interface IndexedEntriesUpdateDAO extends DBConnectionDAO {
    String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (SERIAL_NUMBER INTEGER PRIMARY KEY, ENTRY JSONB)")
    void ensureIndexedEntriesTableExists();

    @SqlQuery("SELECT MAX(SERIAL_NUMBER) FROM " + INDEXED_ENTRIES_TABLE)
    int lastReadSerialNumber();

    @SqlBatch("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(SERIAL_NUMBER, ENTRY) VALUES(:serial_number, :content)")
    void writeBatch(@BindBean Iterator<OrderedIndexEntry> orderedIndexEntry);
}
