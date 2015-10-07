package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderedEntryIndexMapper implements ResultSetMapper<OrderedEntryIndex> {
    @Override
    public OrderedEntryIndex map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new OrderedEntryIndex(r.getInt("serial_number"), r.getString("ENTRY"));

    }
}
