package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@UseStringTemplate3StatementLocator
public interface SourceDBQueryDAO extends DBConnectionDAO {
    String ENTRIES_TABLE = "entries";

    @RegisterMapper(FatEntryMapper.class)
    @SqlQuery("SELECT ID,ENTRY FROM " + ENTRIES_TABLE + " WHERE ID > :lastReadSerialNumber ORDER BY ID LIMIT 5000")
    List<FatEntry> read(@Bind("lastReadSerialNumber") int lastReadSerialNumber);

    @RegisterMapper(RecordMapper.class)
    @SqlQuery("SELECT entry_number, entry.sha256hex as sha256hex, timestamp, content FROM item, entry WHERE item.sha256hex=entry.sha256hex and entry_number > :lastReadEntryNumber ORDER BY entry_number LIMIT 5000")
    List<Record> readRecords(@Bind("lastReadEntryNumber") int lastReadEntryNumber);

    class FatEntryMapper implements ResultSetMapper<FatEntry> {
        @Override
        public FatEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new FatEntry(
                    r.getInt("ID"),
                    r.getBytes("ENTRY")
            );
        }
    }

    class RecordMapper implements ResultSetMapper<Record> {
        @Override
        public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Record(
                    new Entry(
                            r.getInt("entry_number"),
                            r.getString("sha256hex"),
                            r.getTimestamp("timestamp")
                    ),
                    new Item(
                            r.getString("sha256hex"),
                            r.getBytes("content")
                    )
            );
        }
    }
}
