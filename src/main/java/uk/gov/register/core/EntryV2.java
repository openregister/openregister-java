package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.register.util.HashValue;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"index-entry-number", "entry-number", "entry-timestamp", "key", "blob-hash"})
public class EntryV2 extends BaseEntry {
    public EntryV2(int entryNumber, HashValue hashValue, Instant timestamp, String key, EntryType entryType) {
        super(entryNumber, hashValue, timestamp, key, entryType);
    }

    public EntryV2(int entryNumber, List<HashValue> hashValues, Instant timestamp, String key, EntryType entryType) {
        super(entryNumber, hashValues, timestamp, key, entryType);
    }

    public EntryV2(int indexEntryNumber, int entryNumber, List<HashValue> hashValues, Instant timestamp, String key, EntryType entryType) {
        super(indexEntryNumber, entryNumber, hashValues, timestamp, key, entryType);
    }

    public EntryV2(@JsonProperty("index-entry-number") int indexEntryNumber, @JsonProperty("entry-number") int entryNumber,
                            @JsonProperty("blob-hash") List<HashValue> hashValues, @JsonProperty("entry-timestamp") Instant timestamp,
                            @JsonProperty("key") String key) {
        super(indexEntryNumber, entryNumber, hashValues, timestamp, key);
    }

    @Override
    @JsonProperty("blob-hash")
    public List<HashValue> getBlobHashes() {
        return hashValues;
    }

}
