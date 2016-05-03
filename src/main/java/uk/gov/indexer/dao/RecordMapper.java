package uk.gov.indexer.dao;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordMapper implements ResultSetMapper<Record> {
    @Override
    public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Record(
                new Entry(
                        r.getInt("entry_number"),
                        r.getString("sha256hex"),
                        r.getTimestamp("timestamp")
                ),
                new Item(
                        r.getString("sha256hex"),
                        r.getBytes("content")
                )
        );
    }
}
