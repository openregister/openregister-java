package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

public class LongTimestampToInstantMapper implements ResultSetMapper<Instant> {
    @Override
    public Instant map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return Instant.ofEpochSecond(r.getLong("timestamp"));
    }
}