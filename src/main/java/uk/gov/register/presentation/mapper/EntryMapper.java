package uk.gov.register.presentation.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.presentation.DbContent;
import uk.gov.register.presentation.DbEntry;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EntryMapper implements ResultSetMapper<DbEntry> {
    private final ObjectMapper objectMapper;

    public EntryMapper() {
        objectMapper = Jackson.newObjectMapper();
    }

    @Override
    public DbEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        try {
            return new DbEntry(r.getInt("serial_number"), objectMapper.readValue(r.getBytes("entry"), DbContent.class));
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
