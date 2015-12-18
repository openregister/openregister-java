package uk.gov.store;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;

import java.util.List;

public interface EntriesUpdateDAO {
    String tableName = "entries";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (ID SERIAL PRIMARY KEY, ENTRY BYTEA)")
    void ensureTableExists();

    @SqlBatch("INSERT INTO " + tableName + "(ENTRY) values(:messages)")
    @BatchChunkSize(1000)
    void add(@Bind("messages") List<byte[]> messages);
}
