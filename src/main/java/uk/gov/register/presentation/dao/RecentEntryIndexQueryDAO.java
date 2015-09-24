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

    @SqlQuery("SELECT ID,ENTRY FROM ORDERED_ENTRY_INDEX WHERE ID IN(SELECT ID FROM CURRENT_KEYS ORDER BY ID DESC LIMIT :limit)")
    List<DbEntry> getLatestEntriesOfRecords(@Bind("limit") long maxNumberToFetch);

    @SqlQuery("SELECT COUNT FROM REGISTER_ENTRIES_COUNT")
    int getTotalEntriesCount();
}
