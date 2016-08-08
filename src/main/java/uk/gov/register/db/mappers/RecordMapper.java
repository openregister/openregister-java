package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class RecordMapper implements ResultSetMapper<Record> {
    private final EntryMapper entryMapper;
    private final ItemMapper itemMapper;

    public RecordMapper() {
        this.entryMapper = new EntryMapper();
        this.itemMapper = new ItemMapper();
    }

    @Override
    public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return new Record(
                entryMapper.map(index, r, ctx),
                itemMapper.map(index, r, ctx)
        );
    }
}
