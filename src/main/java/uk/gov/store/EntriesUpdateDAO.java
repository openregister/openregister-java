package uk.gov.store;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlBatch;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface EntriesUpdateDAO {
    String tableName = "entries";

    @SqlUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (ID SERIAL PRIMARY KEY, ENTRY BYTEA)")
    void ensureTableExists();

    @SqlBatch("INSERT INTO " + tableName + "(ENTRY) values(:messages)")
    @BatchChunkSize(1000)
    void add(@Bind("messages") List<byte[]> messages);

    //methods below are temporary purpose to copy all data from entries table to entry and item tables
    @SqlQuery("select max(id) from entries")
    int maxId();

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("SELECT ID,ENTRY FROM " + tableName + " WHERE ID > :lastReadSerialNumber ORDER BY ID LIMIT 5000")
    List<OldSchemaEntry> read(@Bind("lastReadSerialNumber") int lastReadSerialNumber);

    class EntryMapper implements ResultSetMapper<OldSchemaEntry> {
        @Override
        public OldSchemaEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new OldSchemaEntry(
                    r.getInt("ID"),
                    r.getBytes("ENTRY")
            );
        }
    }
}

