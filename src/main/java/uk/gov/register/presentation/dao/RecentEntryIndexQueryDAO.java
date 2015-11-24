package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.presentation.DbEntry;
import uk.gov.register.presentation.mapper.EntryMapper;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RegisterMapper(EntryMapper.class)
public abstract class RecentEntryIndexQueryDAO {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index ORDER BY serial_number DESC LIMIT :limit OFFSET :offset")
    public abstract List<DbEntry> getAllEntries(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index WHERE (entry @> :content) ORDER BY serial_number DESC")
    public abstract List<DbEntry> findAllEntriesByJsonContent(@Bind("content") PGobject json);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index WHERE (entry #>> ARRAY['hash']) = :hash")
    @SingleValueResult(DbEntry.class)
    public abstract Optional<DbEntry> findEntryByHash(@Bind("hash") String hash);

    @SqlQuery("SELECT serial_number,entry from ordered_entry_index where serial_number = :serial")
    @SingleValueResult(DbEntry.class)
    public abstract Optional<DbEntry> findEntryBySerialNumber(@Bind("serial") long serial);

    @SqlQuery("SELECT serial_number,entry FROM (" +
            "SELECT idx.serial_number, idx.entry FROM ordered_entry_index idx, current_keys ck " +
            "WHERE  entry @> :content " +
            "AND idx.serial_number = ck.serial_number " +
            "LIMIT 100" +
            ") q_result " +
            "ORDER BY serial_number DESC")
    public abstract List<DbEntry> findLatestEntriesOfRecordsByJsonContent(@Bind("content") PGobject content);

    @SqlQuery("SELECT serial_number,entry FROM ordered_entry_index " +
            "WHERE serial_number IN(" +
            "SELECT serial_number FROM current_keys ORDER BY serial_number DESC LIMIT :limit OFFSET :offset" +
            ") ORDER BY serial_number DESC")
    public abstract List<DbEntry> getLatestEntriesOfRecords(@Bind("limit") long maxNumberToFetch, @Bind("offset") long offset);

    @SqlQuery("SELECT count FROM total_entries")
    public abstract int getTotalEntries();

    @SqlQuery("SELECT count FROM total_records")
    public abstract int getTotalRecords();

    @SqlQuery("SELECT last_updated FROM total_entries")
    public abstract LocalDateTime getLastUpdatedTime();

    public List<DbEntry> findAllEntriesByKeyValue(String key, String value) throws Exception {
        Object entry = ImmutableMap.of("entry", ImmutableMap.of(key, value));
        return findAllEntriesByJsonContent(writePGObject(entry));
    }

    public List<DbEntry> findLatestEntriesOfRecordsByKeyValue(String key, String value) throws Exception {
        Object entry = ImmutableMap.of("entry", ImmutableMap.of(key, value));
        return findLatestEntriesOfRecordsByJsonContent(writePGObject(entry));
    }

    private PGobject writePGObject(Object value) throws JsonProcessingException, SQLException {
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue(objectMapper.writeValueAsString(value));
        return json;
    }
}
