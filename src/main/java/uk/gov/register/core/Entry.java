package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonPropertyOrder({"entry-number", "entry-timestamp", "item-hash"})
public class Entry {
    private final int entryNumber;
    private final HashValue hashValue;
    private final Instant timestamp;

    public Entry(int entryNumber, String sha256hex, Instant timestamp) {
        this.entryNumber = entryNumber;
        this.hashValue = new HashValue(HashingAlgorithm.SHA256, sha256hex);
        this.timestamp = timestamp;
    }

    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unused, used from DAO")
    @JsonIgnore
    public String getSha256hex() {
        return hashValue.decode();
    }

    @JsonIgnore
    public long getTimestampAsLong() {
        return timestamp.getEpochSecond();
    }

    @JsonProperty("entry-number")
    @JsonSerialize(using = ToStringSerializer.class)
    public Integer getEntryNumber() {
        return entryNumber;
    }

    @JsonProperty("item-hash")
    public String getItemHash() {
        return hashValue.encode();
    }

    @JsonProperty("entry-timestamp")
    public String getTimestampAsISOFormat() {
        return ISODateFormatter.format(timestamp);
    }

    public static CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(Entry.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (entryNumber != entry.entryNumber) return false;
        return hashValue == null ? entry.hashValue == null : hashValue.decode().equals(entry.hashValue.decode());
    }

    @Override
    public int hashCode() {
        String hash = hashValue.decode();
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * entryNumber + result;
        return result;
    }
}