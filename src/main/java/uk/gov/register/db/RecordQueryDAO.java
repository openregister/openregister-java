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
import java.util.List;
import java.util.Optional;

public abstract class RecordQueryDAO {
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SqlQuery("SELECT count FROM total_records")
    public abstract int getTotalRecords();

    @SqlQuery("select e.entry_number, array_agg(ei.sha256hex) as sha256hex, e.timestamp, e.key, array_agg(i.content) as content from entry e join entry_item ei on ei.entry_number = e.entry_number and e.entry_number = (select entry_number from current_keys where current_keys.key=:key) join item i on i.sha256hex = ei.sha256hex group by e.entry_number")
    @SingleValueResult(Record.class)
    @RegisterMapper(RecordMapper.class)
    public abstract Optional<Record> findByPrimaryKey(@Bind("key") String key);

    @SqlQuery("select e.entry_number, array_agg(ei.sha256hex) as sha256hex, e.timestamp, e.key, array_agg(i.content) as content from entry e join entry_item ei on ei.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex join current_keys ck on ck.entry_number = e.entry_number group by e.entry_number order by e.entry_number desc limit :limit offset :offset")
    @RegisterMapper(RecordMapper.class)
    public abstract List<Record> getRecords(@Bind("limit") long limit, @Bind("offset") long offset);

    @SqlQuery("select e.entry_number, array_remove(array_agg(ei.sha256hex), null) as sha256hex, e.timestamp, e.key from entry e left join entry_item ei on ei.entry_number = e.entry_number where e.key = :key group by e.entry_number order by e.entry_number asc")
    @RegisterMapper(EntryMapper.class)
    public abstract List<Entry> findAllEntriesOfRecordBy(@Bind("key") String key);

    @SqlQuery("select e.entry_number, array_agg(ei.sha256hex) as sha256hex, e.timestamp, e.key, array_agg(i.content) as content from entry e join entry_item ei on ei.entry_number = e.entry_number join item i on i.sha256hex = ei.sha256hex join current_keys ck on ck.entry_number = e.entry_number where i.content @> :contentPGobject group by e.entry_number limit 100")
    @RegisterMapper(RecordMapper.class)
    public abstract List<Record> __findMax100RecordsByKeyValue(@Bind("contentPGobject") PGobject content);

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
