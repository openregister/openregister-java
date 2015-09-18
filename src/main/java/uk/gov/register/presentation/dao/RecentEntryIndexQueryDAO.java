package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.mapper.EntryMapper;

import java.util.List;
import java.util.Optional;

@RegisterMapper(EntryMapper.class)
public interface RecentEntryIndexQueryDAO {

    @SqlQuery("SELECT id,entry FROM ordered_entry_index ORDER BY id DESC LIMIT :limit OFFSET :offset")
    List<DbEntry> getAllEntries(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);

    @SqlQuery("SELECT id,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY id DESC limit 1")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT id,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY id DESC")
    List<DbEntry> findAllByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT id,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['hash']) = :hash")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findByHash(@Bind("hash") String hash);

    @SqlQuery("SELECT id,entry FROM ordered_entry_index WHERE id = :serial")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findBySerial(@Bind("serial") long serial);

    @SqlQuery("SELECT i.id, i.entry FROM ordered_entry_index i, ( " +
            "SELECT MAX(id) AS id " +
            "FROM ordered_entry_index " +
            "GROUP BY (entry #>> ARRAY['entry',:key])) AS ii " +
            "WHERE i.id = ii.id " +
            "ORDER BY (entry #>> ARRAY['entry',:key]) DESC LIMIT :limit OFFSET :offset")
    List<DbEntry> getLatestEntriesOfRecords(@Bind("key") String name,
                                            @Bind("limit") long maxNumberToFetch,
                                            @Bind("offset") long offset);

    //TODO: change this query as it will die in large registers
    @SqlQuery("SELECT COUNT(*) FROM ORDERED_ENTRY_INDEX")
    long getTotalEntriesCount();
}
