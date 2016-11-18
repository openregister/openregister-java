package uk.gov.register.functional.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.base.Throwables;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.time.Instant;

public class TestEntry {
    private static final ObjectMapper objectMapper = new ObjectMapper() {{
        configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }};

    public final int entryNumber;
    public final String itemJson;
    public final Instant entryTimestamp;
    public final String sha256hex;
    public final String itemKey;


    private TestEntry(int entryNumber, String itemJson, Instant entryTimestamp, String itemKey) {
        try {
            this.entryNumber = entryNumber;
            this.entryTimestamp = entryTimestamp;
            this.itemJson = canonicalJson(itemJson);
            this.sha256hex = DigestUtils.sha256Hex(this.itemJson);
            this.itemKey = itemKey;
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public long getTimestampAsLong() {
        return entryTimestamp.getEpochSecond();
    }

    public static TestEntry anEntry(int entryNumber, String itemJson, String itemKey) {
        return new TestEntry(entryNumber, itemJson, Instant.now(), itemKey);
    }

    public static TestEntry anEntry(int entryNumber, String itemJson, Instant entryTimestamp, String itemKey) {
        return new TestEntry(entryNumber, itemJson, entryTimestamp, itemKey);
    }

    private String canonicalJson(String itemJson) throws IOException {
        return objectMapper.writeValueAsString(
                objectMapper.treeToValue(
                        objectMapper.readTree(itemJson), Object.class
                )
        );
    }
}
