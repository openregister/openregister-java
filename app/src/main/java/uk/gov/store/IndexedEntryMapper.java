package uk.gov.store;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IndexedEntryMapper implements ResultSetMapper<IndexedEntry> {
    @Override
    public IndexedEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new IndexedEntry(r.getInt("ID"), r.getBytes("ENTRY"));
    }
}
