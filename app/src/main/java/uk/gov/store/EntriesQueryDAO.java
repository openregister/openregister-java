package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.Iterator;

@RegisterMapper(IndexedEntryMapper.class)
public interface EntriesQueryDAO {
    String tableName = EntriesUpdateDAO.tableName;

    @SqlQuery("SELECT ID, ENTRY FROM " + tableName + " WHERE ID > :high_water_mark ORDER BY ID")
    Iterator<IndexedEntry> getEntriesSince(@Bind("high_water_mark") int highWaterMark);

    void close();
}
