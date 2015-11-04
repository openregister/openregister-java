package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface IndexedEntriesUpdateDAO extends DBConnectionDAO {
    String INDEXED_ENTRIES_TABLE = "ordered_entry_index";
    String INDEXED_ENTRIES_INDEX = INDEXED_ENTRIES_TABLE + "_gin";

    String TOTAL_ENTRIES_TABLE = "total_entries";

    String TOTAL_ENTRIES_FUNCTION = TOTAL_ENTRIES_TABLE + "_fn()";
    String TOTAL_ENTRIES_TRIGGER = TOTAL_ENTRIES_TABLE + "_trigger";


    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS " + INDEXED_ENTRIES_TABLE + " (serial_number INTEGER PRIMARY KEY, entry JSONB);" +

            "CREATE TABLE IF NOT EXISTS " + TOTAL_ENTRIES_TABLE + " (count INTEGER);" +

            "INSERT INTO " + TOTAL_ENTRIES_TABLE + "(count) SELECT (SELECT count FROM register_entries_count LIMIT 1)  WHERE NOT EXISTS(SELECT 1 FROM " + TOTAL_ENTRIES_TABLE + ");" +

            "CREATE OR REPLACE FUNCTION " + TOTAL_ENTRIES_FUNCTION + " RETURNS TRIGGER\n" +
            "AS $$\n" +
            "BEGIN\n" +
            "  IF TG_OP = 'INSERT' THEN\n" +
            "     EXECUTE 'UPDATE " + TOTAL_ENTRIES_TABLE + " SET COUNT=COUNT + 1';\n" +
            "     RETURN NEW;\n" +
            "  END IF;\n" +
            "  RETURN NULL;\n" +
            "  END;\n" +
            "$$ LANGUAGE plpgsql;" +

            "DROP TRIGGER IF EXISTS " + TOTAL_ENTRIES_TRIGGER + " ON " + INDEXED_ENTRIES_TABLE + ";" +

            "CREATE TRIGGER " + TOTAL_ENTRIES_TRIGGER + " \n" +
            " AFTER INSERT ON " + INDEXED_ENTRIES_TABLE +
            " FOR EACH ROW EXECUTE PROCEDURE " + TOTAL_ENTRIES_FUNCTION + ";"
    )
    void ensureIndexedEntriesTableExists();

    @SqlUpdate("CREATE INDEX " + INDEXED_ENTRIES_INDEX + " ON " + INDEXED_ENTRIES_TABLE + " USING gin(entry jsonb_path_ops)")
    void createIndexedEntriesIndex();

    @SqlQuery("SELECT 1 FROM pg_indexes WHERE indexname='" + INDEXED_ENTRIES_INDEX + "'")
    boolean indexedEntriesIndexExists();

    @SqlQuery("SELECT MAX(serial_number) FROM " + INDEXED_ENTRIES_TABLE)
    int lastReadSerialNumber();

    @SqlBatch("INSERT INTO " + INDEXED_ENTRIES_TABLE + "(serial_number, entry) VALUES(:serial_number, :dbEntry)")
    void writeBatch(@BindBean Iterable<OrderedEntryIndex> orderedIndexEntry);

    @RegisterMapper(OrderedEntryIndexMapper.class)
    @SqlQuery("SELECT * FROM " + INDEXED_ENTRIES_TABLE + " WHERE serial_number > :serial_number ORDER BY serial_number LIMIT 5000")
    List<OrderedEntryIndex> fetchEntriesAfter(@Bind("serial_number") int watermark);
}
