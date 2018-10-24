package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import org.skife.jdbi.v2.unstable.BindIn;
import uk.gov.register.core.Entry;
import uk.gov.register.db.mappers.EntryMapper;
import uk.gov.register.db.mappers.LongTimestampToInstantMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@UseStringTemplate3StatementLocator
public interface EntryQueryDAO {
    @RegisterMapper(EntryMapper.class)
    @SingleValueResult(Entry.class)
    @SqlQuery("select * from \"<schema>\".entry where entry_number = :entryNumber")
    Optional<Entry> findByEntryNumber(@Bind("entryNumber") int entryNumber, @Define("schema") String schema);

    @RegisterMapper(LongTimestampToInstantMapper.class)
    @SingleValueResult(Instant.class)
    @SqlQuery("SELECT timestamp FROM \"<schema>\".entry ORDER BY entry_number DESC LIMIT 1")
    Optional<Instant> getLastUpdatedTime(@Define("schema") String schema);

    @SqlQuery("SELECT value FROM \"<schema>\".current_entry_number")
    int getTotalEntries(@Define("schema") String schema);

    @SqlQuery("SELECT count(1) FROM \"<schema>\".entry_system")
    int getTotalSystemEntries(@Define("schema") String schema);

    //Note: This is fine for small data registers like country
    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select * from \"<schema>\".entry order by entry_number desc")
    Collection<Entry> getAllEntriesNoPagination(@Define("schema") String schema);

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select * from \"<schema>\".entry where entry_number >= :start and entry_number \\< :start + :limit order by entry_number asc")
    Collection<Entry> getEntries(@Bind("start") int start, @Bind("limit") int limit, @Define("schema") String schema);

    @SqlQuery("select * from \"<schema>\".entry where key = :key order by entry_number asc")
    @RegisterMapper(EntryMapper.class)
    Collection<Entry> getAllEntriesByKey(@Bind("key") String key, @Define("schema") String schema);

    @RegisterMapper(EntryMapper.class)
    @SingleValueResult(Entry.class)
    @SqlQuery("select * from \"<schema>\".<entry_table> where key in (<keys>) order by entry_number asc")
    Collection<Entry> getEntriesByKeys(@BindIn("keys") List<String> entryKeys, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select * from \"<schema>\".entry where entry_number >= :entryNumber order by entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(262144)
        // Has to be non-zero to enable cursor mode https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
    ResultIterator<Entry> entriesIteratorFrom(@Bind("entryNumber") int entryNumber, @Define("schema") String schema);

    @SqlQuery("select * from \"<schema>\".<entry_table> order by entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(10000)
    Iterator<Entry> getIterator(@Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select * from \"<schema>\".<entry_table> where entry_number > :totalEntries1 and entry_number \\<= :totalEntries2 order by entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(10000)
    Iterator<Entry> getIterator(@Bind("totalEntries1") int totalEntries1, @Bind("totalEntries2") int totalEntries2, @Define("schema") String schema, @Define("entry_table") String entryTable);
}
