package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface IndexedEntriesUpdateDAO extends DBConnectionDAO {
    String INDEXED_ENTRIES_TABLE = "ORDERED_ENTRY_INDEX";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (SERIAL_NUMBER INTEGER PRIMARY KEY, ENTRY JSONB)")
    void ensureIndexedEntriesTableExists();

    @SqlQuery("SELECT MAX(SERIAL_NUMBER) FROM " + INDEXED_ENTRIES_TABLE)
    int lastReadSerialNumber();

    @SqlBatch("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(SERIAL_NUMBER, ENTRY) VALUES(:serial_number, :dbEntry)")
    void writeBatch(@BindBean Iterable<OrderedEntryIndex> orderedIndexEntry);

    @RegisterMapper(OrderedEntryIndexMapper.class)
    @SqlQuery("SELECT * FROM " + INDEXED_ENTRIES_TABLE + " WHERE SERIAL_NUMBER > :serial_number ORDER BY SERIAL_NUMBER LIMIT 5000")
    List<OrderedEntryIndex> fetchEntriesAfter(@Bind("serial_number") int watermark);
}
