package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EntryMapper implements ResultSetMapper<Entry> {

    @Override
    public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Entry(r.getInt("ID"), r.getBytes("ENTRY"));

    }
}
