package uk.gov.register.core;

import com.fasterxml.jackson.annotation.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;
import uk.gov.register.util.ToArrayConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"index-entry-number", "entry-number", "entry-timestamp", "key", "item-hash"})
public class Entry implements CsvRepresentationView<Entry> {
    private final int entryNumber;
    private final HashValue hashValue;
    private final Instant timestamp;
    private final EntryType entryType;
    private String key;

    public Entry(int entryNumber, HashValue hashValue, Instant timestamp, String key, EntryType entryType) {
        this.entryNumber = entryNumber;
        this.hashValue = hashValue;
        this.timestamp = timestamp;
        this.key = key;
        this.entryType = entryType;
    }

    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    @JsonProperty("item-hash")
    @JsonSerialize(using = ToArrayConverter.class)
    public HashValue getItemHash() {
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

    @JsonProperty("index-entry-number")
    @JsonSerialize(using = ToStringSerializer.class)
    public Integer getIndexEntryNumber() {
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

    @JsonIgnore
    public EntryType getEntryType() {
        return entryType;
    }

    public static CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(Entry.class);
    }

    public static CsvSchema csvSchemaWithOmittedFields(List<String> fieldsToRemove) {
        CsvSchema originalSchema = csvSchema();
        Iterator<CsvSchema.Column> columns = originalSchema.rebuild().getColumns();

        List<CsvSchema.Column> updatedColumns = Lists.newArrayList(columns);
        updatedColumns.removeIf(c -> fieldsToRemove.contains(c.getName()));

        return CsvSchema.builder().addColumns(updatedColumns).build();
    }

    @Override
    public CsvRepresentation<Entry> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (entryNumber != entry.entryNumber) return false;
        if (key != null ? !key.equals(entry.key) : entry.key != null) return false;
        if (timestamp != null ? !timestamp.equals(entry.timestamp) : entry.timestamp != null) return false;
        if (entryType != null ? !entryType.equals(entry.entryType) : entry.entryType != null) return false;
        return hashValue != null ? hashValue.equals(entry.hashValue) : entry.hashValue == null;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + entryNumber;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (entryType != null ? entryType.hashCode() : 0);
        result = 31 * result + (hashValue != null ? hashValue.hashCode() : 0);

        return result;
    }
}
