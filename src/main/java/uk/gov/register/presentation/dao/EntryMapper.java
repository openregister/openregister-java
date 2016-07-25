package uk.gov.register.presentation.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

public class EntryMapper implements ResultSetMapper<Entry> {
    @Override
    public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        return new Entry(r.getString("entry_number"), r.getString("sha256hex"), r.getTimestamp("timestamp", cal).toInstant());
    }
}
