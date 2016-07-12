package uk.gov.register.presentation.dao;

import io.dropwizard.java8.jdbi.args.InstantMapper;
import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

public interface EntryQueryDAO extends Transactional<EntryQueryDAO> {
    @SqlQuery("select * from entry where entry_number=:entry_number")
    @RegisterMapper(EntryMapper.class)
    @SingleValueResult(Entry.class)
    Optional<Entry> findByEntryNumber(@Bind("entry_number") int entryNumber);

    @RegisterMapper(InstantMapper.class)
    @SqlQuery("SELECT timestamp FROM entry ORDER BY entry_number DESC LIMIT 1")
    Instant getLastUpdatedTime();

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
