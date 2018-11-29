package uk.gov.register.views;

import com.fasterxml.jackson.annotation.*;
import uk.gov.register.core.Entry;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;
import uk.gov.register.util.ToArrayConverter;
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
public class EntryView implements CsvRepresentationView<EntryView> {
    private final int entryNumber;
    private final HashValue hashValue;
    private final Instant timestamp;
    private String key;

    public EntryView(Entry entry) {
        this.entryNumber = entry.getEntryNumber();
        this.hashValue = entry.getV1ItemHash();
        this.timestamp = entry.getTimestamp();
        this.key = entry.getKey();
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

    public static CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(EntryView.class);
    }

    public static CsvSchema csvSchemaWithOmittedFields(List<String> fieldsToRemove) {
        CsvSchema originalSchema = csvSchema();
        Iterator<CsvSchema.Column> columns = originalSchema.rebuild().getColumns();

        List<CsvSchema.Column> updatedColumns = Lists.newArrayList(columns);
        updatedColumns.removeIf(c -> fieldsToRemove.contains(c.getName()));

        return CsvSchema.builder().addColumns(updatedColumns).build();
    }

    @Override
    public CsvRepresentation<EntryView> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), this);
    }
}
