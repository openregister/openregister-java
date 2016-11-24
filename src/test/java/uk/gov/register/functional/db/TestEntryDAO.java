package uk.gov.register.functional.db;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Entry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public interface TestEntryDAO {
    @SqlUpdate("delete from entry;" +
            "delete from current_entry_number;" +
            "insert into current_entry_number values(0);")
    void wipeData();

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select entry_number, sha256hex, timestamp, key from entry")
    List<Entry> getAllEntries();

    @SqlUpdate("insert into entry(entry_number, sha256hex, timestamp, key) values(:entry_number, :sha256hex, :timestamp, :key)")
    void insert(@Bind("entry_number") int serialNumber, @Bind("sha256hex") String sha256, @Bind("timestamp") long timestamp, @Bind("key") String key);


    class EntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            return new Entry(r.getInt("entry_number"), r.getString("sha256hex"), Instant.ofEpochSecond(r.getLong("timestamp")), r.getString("key"));
        }
    }
}
