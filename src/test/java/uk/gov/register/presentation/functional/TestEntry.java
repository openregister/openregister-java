package uk.gov.register.presentation.functional;

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

    private TestEntry(int entryNumber, String itemJson, Instant entryTimestamp) {
        try {
            this.entryNumber = entryNumber;
            this.entryTimestamp = entryTimestamp;
            this.itemJson = canonicalJson(itemJson);
            this.sha256hex = DigestUtils.sha256Hex(this.itemJson);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static TestEntry anEntry(int entryNumber, String itemJson) {
        return new TestEntry(entryNumber, itemJson, Instant.now());
    }

    public static TestEntry anEntry(int entryNumber, String itemJson, Instant entryTimestamp) {
        return new TestEntry(entryNumber, itemJson, entryTimestamp);
    }

    private String canonicalJson(String itemJson) throws IOException {
        return objectMapper.writeValueAsString(
                objectMapper.treeToValue(
                        objectMapper.readTree(itemJson), Object.class
                )
        );
    }
}
