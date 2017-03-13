package uk.gov.register.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Record;
import uk.gov.register.db.mappers.EntryMapper;
import uk.gov.register.db.mappers.RecordMapper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class RecordQueryDAO {

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SqlQuery("SELECT count FROM total_records")
    public abstract int getTotalRecords();

    @SqlQuery("select e.entry_number, e.timestamp, ei.sha256hex as sha256hex, e.key, i.content from entry e join entry_item ei on ei.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex and e.entry_number = (select entry_number from current_keys where current_keys.key=:key)")
    @SingleValueResult(Record.class)
    @RegisterMapper(RecordMapper.class)
    public abstract Optional<Record> findByPrimaryKey(@Bind("key") String key);

    @SqlQuery("select e.entry_number, e.timestamp, ei.sha256hex as sha256hex, e.key, i.content from entry e join entry_item ei on ei.entry_number = e.entry_number join current_keys ck on ck.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex order by e.entry_number desc limit :limit offset :offset")
    @RegisterMapper(RecordMapper.class)
    public abstract List<Record> getRecords(@Bind("limit") long limit, @Bind("offset") long offset);

    @SqlQuery("select e.entry_number, e.timestamp, ei.sha256hex, e.key from entry e join entry_item ei on ei.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex and i.content @> :contentPGobject order by e.entry_number asc")
    @RegisterMapper(EntryMapper.class)
    public abstract Collection<Entry> __findAllEntriesOfRecordBy(@Bind("contentPGobject") PGobject content);

    @SqlQuery("select e.entry_number, e.timestamp, ei.sha256hex as sha256hex, e.key, i.content from entry e join entry_item ei on ei.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex join current_keys ck on ck.entry_number = e.entry_number where i.content @> :contentPGobject and i.sha256hex = ei.sha256hex limit 100")
    @RegisterMapper(RecordMapper.class)
    public abstract List<Record> __findMax100RecordsByKeyValue(@Bind("contentPGobject") PGobject content);

    public Collection<Entry> findAllEntriesOfRecordBy(String key, String value) {
        return __findAllEntriesOfRecordBy(writePGObject(key, value));
    }

    public List<Record> findMax100RecordsByKeyValue(String key, String value) {
        return __findMax100RecordsByKeyValue(writePGObject(key, value));
    }


    private PGobject writePGObject(String key, String value) {
        try {
            PGobject json = new PGobject();
            json.setType("jsonb");
            json.setValue(objectMapper.writeValueAsString(ImmutableMap.of(key, value)));
            return json;
        } catch (SQLException | JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }
}
