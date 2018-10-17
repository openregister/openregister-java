package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.BaseEntry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EntryMapper implements ResultSetMapper<BaseEntry> {
    private final LongTimestampToInstantMapper longTimestampToInstantMapper;

    public EntryMapper() {
        this.longTimestampToInstantMapper = new LongTimestampToInstantMapper();
    }

    @Override
    public BaseEntry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        List<HashValue> hashes = Arrays.asList((String[]) r.getArray("sha256hex").getArray()).stream().map(h -> new HashValue(HashingAlgorithm.SHA256, h)).collect(Collectors.toList());

        return new BaseEntry(r.getInt("entry_number"), hashes, longTimestampToInstantMapper.map(index, r, ctx), r.getString("key"), EntryType.valueOf(r.getString("type")));
    }
}
