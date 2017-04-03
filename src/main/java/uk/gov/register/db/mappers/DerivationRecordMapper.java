package uk.gov.register.db.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.register.core.HashingAlgorithm.SHA256;


public class DerivationRecordMapper implements ResultSetMapper<Record> {
    private final LongTimestampToInstantMapper longTimestampToInstantMapper;
    private final ObjectMapper objectMapper;

    public DerivationRecordMapper() {
        this.longTimestampToInstantMapper = new LongTimestampToInstantMapper();
        this.objectMapper = Jackson.newObjectMapper();
    }

    @Override
    public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        String key = r.getString("key");
        int indexEntryNumber = r.getInt("index_entry_number");
        int entryNumber = r.getInt("entry_number");
        Instant timestamp = longTimestampToInstantMapper.map(index, r, ctx);
        Array shaArray = r.getArray("sha256_arr");
        String[] hashes = (shaArray != null) ? (String[]) shaArray.getArray() : new String[]{};

        Array contentArray = r.getArray("content_arr");
        String[] content = (contentArray != null) ? (String[]) contentArray.getArray() : new String[]{};

        if (hashes.length != content.length) {
            throw new IllegalStateException("query returned unequal number of hashes and content");
        }

        List<HashValue> hashValues = new ArrayList<>();
        List<Item> items = new ArrayList<>();

        for (int i = 0; i < hashes.length; i++) {
            HashValue hashValue = new HashValue(SHA256, hashes[i]);
            hashValues.add(hashValue);
            Item item = new Item(hashValue, jsonNode(content[i]));
            items.add(item);
        }

        Entry entry = new Entry(indexEntryNumber, entryNumber, hashValues, timestamp, key);

        return new Record(entry, items);
    }

    private JsonNode jsonNode(String s) {
        try {
            return objectMapper.readValue(s, JsonNode.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
