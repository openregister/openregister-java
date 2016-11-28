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
@JsonPropertyOrder({"entry-number", "entry-timestamp", "item-hash", "key"})
public class Entry {
    private final int entryNumber;
    private final HashValue hashValue;
    private final Instant timestamp;
    private String key;

    public Entry(int entryNumber, HashValue hashValue, Instant timestamp, String key) {
        this.entryNumber = entryNumber;
        this.hashValue = hashValue;
        this.timestamp = timestamp;
        this.key = key;
    }

    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    @SuppressWarnings("unused, used from DAO")
    @JsonProperty("item-hash")
    @JsonSerialize(using = ToStringSerializer.class)
    public HashValue getSha256hex() {
        return hashValue;
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

    @JsonProperty("entry-timestamp")
    public String getTimestampAsISOFormat() {
        return ISODateFormatter.format(timestamp);
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
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
        return hashValue == null ? entry.hashValue == null : hashValue.equals(entry.hashValue);
    }

    @Override
    public int hashCode() {
        int result = hashValue != null ? hashValue.hashCode() : 0;
        result = 31 * entryNumber + result;
        return result;
    }
}