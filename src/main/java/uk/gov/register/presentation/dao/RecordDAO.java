package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public interface RecordDAO {
    @SqlQuery("select entry_number, timestamp, e.sha256hex as sha256hex, content from entry e, item i where e.sha256hex=i.sha256hex and e.entry_number = (select serial_number from current_keys where current_keys.key=:key)")
    @SingleValueResult(Record.class)
    @RegisterMapper(RecordMapper.class)
    Optional<Record> findBy(@Bind("key") String key);

    class RecordMapper implements ResultSetMapper<Record> {

        private static final ObjectMapper objectMapper = Jackson.newObjectMapper();

        @Override
        public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            try {
                return new Record(
                        new Entry(
                                r.getString("entry_number"),
                                r.getString("sha256hex"),
                                r.getTimestamp("timestamp")
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
}
