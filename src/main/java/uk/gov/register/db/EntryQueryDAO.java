package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.core.Entry;
import uk.gov.register.db.mappers.EntryMapper;
import uk.gov.register.db.mappers.LongTimestampToInstantMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface EntryQueryDAO {
    @RegisterMapper(EntryMapper.class)
    @SingleValueResult(Entry.class)
    @SqlQuery("select * from entry where entry_number=:entryNumber")
    Optional<Entry> findByEntryNumber(@Bind("entryNumber") int entryNumber);

    @RegisterMapper(LongTimestampToInstantMapper.class)
    @SingleValueResult(Instant.class)
    @SqlQuery("SELECT timestamp FROM entry ORDER BY entry_number DESC LIMIT 1")
    Optional<Instant> getLastUpdatedTime();

    @SqlQuery("SELECT value FROM current_entry_number")
    int getTotalEntries();

    //Note: This is fine for small data registers like country
    @RegisterMapper(EntryMapper.class)
    @SqlQuery("SELECT * from entry order by entry_number desc")
    Collection<Entry> getAllEntriesNoPagination();

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select * from entry where entry_number >= :start and entry_number \\< :start + :limit order by entry_number asc")
    Collection<Entry> getEntries(@Bind("start") int start, @Bind("limit") int limit);

    @SqlQuery("SELECT * FROM entry WHERE entry_number >= :entryNumber ORDER BY entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(262144) // Has to be non-zero to enable cursor mode https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
    ResultIterator<Entry> entriesIteratorFrom(@Bind("entryNumber") int entryNumber);
}
