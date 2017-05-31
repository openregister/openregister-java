package uk.gov.register.db;

import org.skife.jdbi.v2.ResultIterator;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.FetchSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Entry;
import uk.gov.register.db.mappers.EntryMapper;
import uk.gov.register.db.mappers.LongTimestampToInstantMapper;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

@UseStringTemplate3StatementLocator
public interface EntryQueryDAO {
    @RegisterMapper(EntryMapper.class)
    @SingleValueResult(Entry.class)
    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number where e.entry_number = :entryNumber group by e.entry_number")
    Optional<Entry> findByEntryNumber(@Bind("entryNumber") int entryNumber, @Define("schema") String schema );

    @RegisterMapper(LongTimestampToInstantMapper.class)
    @SingleValueResult(Instant.class)
    @SqlQuery("SELECT timestamp FROM \"<schema>\".entry ORDER BY entry_number DESC LIMIT 1")
    Optional<Instant> getLastUpdatedTime( @Define("schema") String schema );

    @SqlQuery("SELECT value FROM \"<schema>\".current_entry_number")
    int getTotalEntries( @Define("schema") String schema );

    //Note: This is fine for small data registers like country
    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number group by e.entry_number order by e.entry_number desc")
    Collection<Entry> getAllEntriesNoPagination( @Define("schema") String schema );

    @RegisterMapper(EntryMapper.class)
    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number where e.entry_number >= :start and e.entry_number \\< :start + :limit group by e.entry_number order by e.entry_number asc")
    Collection<Entry> getEntries(@Bind("start") int start, @Bind("limit") int limit, @Define("schema") String schema );

    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number where e.entry_number >= :entryNumber group by e.entry_number order by e.entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(262144) // Has to be non-zero to enable cursor mode https://jdbc.postgresql.org/documentation/head/query.html#query-with-cursor
    ResultIterator<Entry> entriesIteratorFrom(@Bind("entryNumber") int entryNumber, @Define("schema") String schema );

    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number group by e.entry_number order by e.entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(10000)
    Iterator<Entry> getIterator( @Define("schema") String schema );

    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key, e.type from \"<schema>\".entry e left join \"<schema>\".entry_item ei on ei.entry_number = e.entry_number where e.entry_number > :totalEntries1 and e.entry_number \\<= :totalEntries2 group by e.entry_number order by e.entry_number")
    @RegisterMapper(EntryMapper.class)
    @FetchSize(10000)
    Iterator<Entry> getIterator(@Bind("totalEntries1") int totalEntries1, @Bind("totalEntries2") int totalEntries2, @Define("schema") String schema );
}
