package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public abstract class RecordDAO {

    private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SqlQuery("SELECT count FROM total_records")
    public abstract int getTotalRecords();

    @SqlQuery("select entry_number, timestamp, e.sha256hex as sha256hex, content from entry e, item i where e.sha256hex=i.sha256hex and e.entry_number = (select serial_number from current_keys where current_keys.key=:key)")
    @SingleValueResult(Record.class)
    @RegisterMapper(RecordMapper.class)
    public abstract Optional<Record> findBy(@Bind("key") String key);

    @SqlQuery(" select entry_number, timestamp, sha256hex from entry where sha256hex in (select sha256hex from item where (content @> :contentPGobject))")
    @RegisterMapper(NewEntryMapper.class)
    public abstract List<Entry> findAllEntriesOfRecordBy(@Bind("contentPGobject") PGobject content);

    public List<Entry> findAllEntriesOfRecordBy(String key, String value) {
        try {
            return findAllEntriesOfRecordBy(writePGObject(ImmutableMap.of(key, value)));
        } catch (Throwable t) {
            throw Throwables.propagate(t);
        }
    }

    public static class RecordMapper implements ResultSetMapper<Record> {
        @Override
        public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            try {
                return new Record(
                        new Entry(
                                r.getString("entry_number"),
                                r.getString("sha256hex"),
                                r.getTimestamp("timestamp").toInstant()
                        ),
                        new Item(
                                r.getString("sha256hex"),
                                objectMapper.readTree(r.getString("content"))
                        )
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private PGobject writePGObject(Object value) throws JsonProcessingException, SQLException {
        PGobject json = new PGobject();
        json.setType("jsonb");
        json.setValue(objectMapper.writeValueAsString(value));
        return json;
    }
}
