package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

public interface SourceDBQueryDAO extends DBConnectionDAO {
    String ENTRIES_TABLE = "entries";

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("SELECT ID,ENTRY FROM " + ENTRIES_TABLE + " WHERE ID > :lastReadSerialNumber ORDER BY ID LIMIT 5000")
    List<Entry> read(@Bind("lastReadSerialNumber") int lastReadSerialNumber);

    @SqlQuery("SELECT MAX(ID) FROM " + ENTRIES_TABLE)
    int lastEntryID();
}
