package uk.gov.register.views.v2;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import uk.gov.register.util.ToArrayConverter;
import uk.gov.register.views.CsvRepresentationView;
import uk.gov.register.views.representations.CsvRepresentation;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"index-entry-number", "entry-number", "entry-timestamp", "key", "blob-hash"})
public class EntryView implements CsvRepresentationView<EntryView> {
    private final int entryNumber;
    private final HashValue hashValue;
    private final Instant timestamp;
    private String key;

    public EntryView(Entry entry) {
        this.entryNumber = entry.getEntryNumber();
        this.hashValue = entry.getItemHash();
        this.timestamp = entry.getTimestamp();
        this.key = entry.getKey();
    }

    @JsonIgnore
    public Instant getTimestamp() {
        return timestamp;
    }

    @JsonProperty("blob-hash")
    public HashValue getBlobHash() {
        return hashValue;
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
        return csvMapper.schemaFor(EntryView.class);
    }

    @Override
    public CsvRepresentation<EntryView> csvRepresentation() {
        return new CsvRepresentation<>(csvSchema(), this);
    }
}
