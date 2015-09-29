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

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index ORDER BY serial_number DESC LIMIT :limit OFFSET :offset")
    List<DbEntry> getAllEntries(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY serial_number DESC limit 1")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['entry',:key]) = :value ORDER BY serial_number DESC")
    List<DbEntry> findAllByKeyValue(@Bind("key") String key, @Bind("value") String value);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['hash']) = :hash")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findByHash(@Bind("hash") String hash);

    @SqlQuery("SELECT serial_number,entry from ordered_entry_index where serial_number = :serial")
    @SingleValueResult(DbEntry.class)
    Optional<DbEntry> findBySerial(@Bind("serial") long serial);

    @SqlQuery("SELECT SERIAL_NUMBER,ENTRY FROM ORDERED_ENTRY_INDEX WHERE SERIAL_NUMBER IN(SELECT SERIAL_NUMBER FROM CURRENT_KEYS ORDER BY SERIAL_NUMBER DESC LIMIT :limit)")
    List<DbEntry> getLatestEntriesOfRecords(@Bind("limit") long maxNumberToFetch);

    @SqlQuery("SELECT COUNT FROM REGISTER_ENTRIES_COUNT")
    int getTotalEntriesCount();
}
