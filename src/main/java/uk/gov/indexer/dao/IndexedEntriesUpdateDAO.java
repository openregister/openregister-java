package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface IndexedEntriesUpdateDAO extends DBConnectionDAO {
    String INDEXED_ENTRIES_TABLE = "ordered_entry_index";
    String INDEXED_ENTRIES_INDEX = INDEXED_ENTRIES_TABLE + "_gin";

    @SqlUpdate("create table if not exists " + INDEXED_ENTRIES_TABLE + " (serial_number integer primary key, entry jsonb)")
    void ensureIndexedEntriesTableExists();


    @SqlUpdate("create index " + INDEXED_ENTRIES_INDEX + " on " + INDEXED_ENTRIES_TABLE + " using gin(entry jsonb_path_ops)")
    void createIndexedEntriesIndex();

    @SqlQuery("select 1 from pg_indexes where indexname='" + INDEXED_ENTRIES_INDEX + "'")
    boolean indexedEntriesIndexExists();

    @SqlQuery("select max(serial_number) from " + INDEXED_ENTRIES_TABLE)
    int lastReadSerialNumber();

    @SqlBatch("insert into " + INDEXED_ENTRIES_TABLE + "(serial_number, entry) values(:serial_number, :dbentry)")
    void writeBatch(@BindBean Iterable<OrderedEntryIndex> orderedIndexEntry);

    @RegisterMapper(OrderedEntryIndexMapper.class)
    @SqlQuery("select * from " + INDEXED_ENTRIES_TABLE + " where serial_number > :serial_number order by serial_number limit 5000")
    List<OrderedEntryIndex> fetchEntriesAfter(@Bind("serial_number") int watermark);
}
