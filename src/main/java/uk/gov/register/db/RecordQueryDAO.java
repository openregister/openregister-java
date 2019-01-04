package uk.gov.register.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.Define;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.sqlobject.stringtemplate.UseStringTemplate3StatementLocator;
import uk.gov.register.core.Record;
import uk.gov.register.db.mappers.RecordMapper;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import static com.google.common.base.Throwables.throwIfUnchecked;

@UseStringTemplate3StatementLocator
public abstract class RecordQueryDAO {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @SqlQuery("select * from (select distinct on (e.key) e.*, i.content from \"<schema>\".<entry_table> e, \"<schema>\".item i " +
            "where e.sha256hex = i.sha256hex order by e.key, e.entry_number desc) as records order by entry_number desc limit :limit offset :offset")
    @RegisterMapper(RecordMapper.class)
    public abstract Collection<Record> getRecords(@Bind("limit") int limit, @Bind("offset") int offset, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select distinct on (e.key) e.*, i.content from \"<schema>\".<entry_table> e, \"<schema>\".item i " +
            "where e.sha256hex = i.sha256hex and e.key = :key order by e.key, e.entry_number desc")
    @RegisterMapper(RecordMapper.class)
    @SingleValueResult(Record.class)
    public abstract Optional<Record> getRecord(@Bind("key") String key, @Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select count(distinct key) from \"<schema>\".<entry_table>")
    public abstract int getTotalRecords(@Define("schema") String schema, @Define("entry_table") String entryTable);

    @SqlQuery("select * from (select distinct on (e.key) e.*, i.content from \"<schema>\".<entry_table> e, \"<schema>\".item i " +
            "where e.sha256hex = i.sha256hex order by e.key, e.entry_number desc) as records where content @> :contentPGobject order by entry_number desc limit 100")
    @RegisterMapper(RecordMapper.class)
    public abstract Collection<Record> __findMax100RecordsByKeyValue(@Bind("contentPGobject") PGobject content, @Bind("key") String key, @Define("schema") String schema, @Define("entry_table") String entryTable);

    public Collection<Record> findMax100RecordsByKeyValue(String key, String value, String schema, String entryTable) {
        return __findMax100RecordsByKeyValue(writePGObject(key, value), key, schema, entryTable);
    }

    private PGobject writePGObject(String key, String value) {
        try {
            PGobject json = new PGobject();
            json.setType("jsonb");
            json.setValue(objectMapper.writeValueAsString(ImmutableMap.of(key, value)));
            return json;
        } catch (SQLException | JsonProcessingException e) {
            throwIfUnchecked(e);
            throw new RuntimeException(e.getCause());
        }
    }
}
