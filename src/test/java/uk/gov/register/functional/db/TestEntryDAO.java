package uk.gov.register.functional.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface TestEntryDAO {
    @SqlUpdate("delete from entry;" +
            "delete from entry_item;" +
            "delete from current_entry_number;" +
            "insert into current_entry_number values(0);")
    void wipeData();

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key from entry e left join entry_item ei on ei.entry_number = e.entry_number group by e.entry_number order by e.entry_number")
    List<Entry> getAllEntries();

    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key from entry e left join entry_item ei on ei.entry_number = e.entry_number where e.entry_number >= :entryNumber group by e.entry_number order by e.entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(262144) // Has to be non-zero to enable cursor mode https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
    ResultIterator<Entry> entriesIteratorFrom(@Bind("entryNumber") int entryNumber);

    class EntryMapper implements ResultSetMapper<Entry> {
        @Override
        public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            List<HashValue> hashes = Arrays.asList((String[]) r.getArray("sha256hex").getArray()).stream().map(h -> new HashValue(HashingAlgorithm.SHA256, h)).collect(Collectors.toList());

            return new Entry(r.getInt("entry_number"), hashes, Instant.ofEpochSecond(r.getLong("timestamp")), r.getString("key"));
        }
    }
}
