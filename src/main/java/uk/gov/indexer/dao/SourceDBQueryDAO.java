package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.indexer.ctserver.CTLeafInputGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface SourceDBQueryDAO extends DBConnectionDAO {
    String ENTRIES_TABLE = "entries";

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("SELECT ID,ENTRY FROM " + ENTRIES_TABLE + " WHERE ID > :lastReadSerialNumber ORDER BY ID LIMIT 5000")
    List<Entry> read(@Bind("lastReadSerialNumber") int lastReadSerialNumber);

    @SqlQuery("SELECT MAX(ID) FROM " + ENTRIES_TABLE)
    int lastEntryID();

    class EntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Entry(
                    r.getInt("ID"),
                    r.getBytes("ENTRY"),
                    CTLeafInputGenerator.createLeafInputFrom(new String(r.getBytes("ENTRY")), System.currentTimeMillis())
            );
        }
    }
}
