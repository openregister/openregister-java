package uk.gov.indexer.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;

import java.util.List;

public interface SourceDBQueryDAO extends DBQueryDAO {
    String ENTRIES_TABLE = "entries";

    @SqlQuery("SELECT ENTRY FROM " + ENTRIES_TABLE + " WHERE ID > :currentWaterMark ORDER BY ID LIMIT 5000")
    List<byte[]> read(@Bind("currentWaterMark") int currentWaterMark);
}

