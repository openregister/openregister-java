package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import uk.gov.register.core.Entry;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"index-entry-number", "entry-number", "entry-timestamp", "key", "blob-hash"})
public class V2EntryView implements CsvRepresentationView<V2EntryView> {
    private final int indexEntryNumber;
    private final int entryNumber;
    private final List<HashValue> hashValues;
    private final Instant timestamp;
    private String key;

    public V2EntryView(Entry entry) {
        this.indexEntryNumber = entry.getIndexEntryNumber();
        this.entryNumber = entry.getEntryNumber();
        this.hashValues = entry.getBlobHashes();
        this.timestamp = entry.getTimestamp();
        this.key = entry.getKey();
    }

    @JsonCreator
    public V2EntryView(@JsonProperty("index-entry-number") int indexEntryNumber, @JsonProperty("entry-number") int entryNumber,
                     @JsonProperty("blob-hash") List<HashValue> hashValues, @JsonProperty("entry-timestamp") Instant timestamp,
                     @JsonProperty("key") String key) {
        this.indexEntryNumber = indexEntryNumber;
        this.entryNumber = entryNumber;
        this.hashValues = hashValues;
        this.timestamp = timestamp;
        this.key = key;
    }

    @JsonProperty("blob-hash")
    public List<HashValue> getBlobHashes() {
        return hashValues;
    }


    @JsonProperty("entry-number")
    @JsonSerialize(using = ToStringSerializer.class)
    public Integer getEntryNumber() {
        return entryNumber;
    }

    @JsonProperty("index-entry-number")
    @JsonSerialize(using = ToStringSerializer.class)
    public Integer getIndexEntryNumber() {
        return indexEntryNumber;
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
        return csvMapper.schemaFor(V2EntryView.class);
    }

    @SuppressWarnings("unused, intended to be used for Record serialization")
    public static CsvSchema csvSchemaWithOmittedFields(List<String> fieldsToRemove) {
        CsvSchema originalSchema = csvSchema();
        Iterator<CsvSchema.Column> columns = originalSchema.rebuild().getColumns();

        List<CsvSchema.Column> updatedColumns = Lists.newArrayList(columns);
        updatedColumns.removeIf(c -> fieldsToRemove.contains(c.getName()));

        return CsvSchema.builder().addColumns(updatedColumns).build();
    }

    @Override
    public CsvRepresentation<V2EntryView> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), this);
    }

}
