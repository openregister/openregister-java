package uk.gov.register.db.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Entry;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordMapper implements ResultSetMapper<Record> {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Override
    public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        try {
            return new Record(
                    new Entry(r.getString("entry_number"), r.getString("sha256hex"), r.getTimestamp("timestamp").toInstant()),
                    new Item(r.getString("sha256hex"), objectMapper.readTree(r.getString("content")))
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
