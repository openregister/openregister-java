package uk.gov.register.db.mappers;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.util.HashValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DerivationEntryMapper implements ResultSetMapper<Entry> {
    private final LongTimestampToInstantMapper longTimestampToInstantMapper;

    public DerivationEntryMapper() {
        this.longTimestampToInstantMapper = new LongTimestampToInstantMapper();
    }

    @Override
    public Entry map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        String key = r.getString("key");
        int indexEntryNumber = r.getInt("index_entry_number");
        int entryNumber = r.getInt("entry_number");
        String entryType = r.getString("type");
        Instant timestamp = longTimestampToInstantMapper.map(index, r, ctx);
        String[] hashes = (String[]) r.getArray("sha256_arr").getArray();

        List<HashValue> hashValues = new ArrayList<>();

        for (int i = 0; i < hashes.length; i++) {
                HashValue hashValue = new HashValue(HashingAlgorithm.SHA256, hashes[i]);
                hashValues.add(hashValue);
        }

        return new Entry(indexEntryNumber, entryNumber, hashValues, timestamp, key, EntryType.valueOf(entryType));
    }

}
