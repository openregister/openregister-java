package uk.gov.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import uk.gov.register.presentation.ISODateFormatter;

import java.time.Instant;

@JsonPropertyOrder({"entry-number","entry-timestamp","item-hash"})
public class Entry {
    @JsonProperty("entry-number")
    public final String entryNumber;

    private final String sha256hex;

    private final Instant timestamp;

    public Entry(String entryNumber, String sha256hex, Instant timestamp) {
        this.entryNumber = entryNumber;
        this.sha256hex = sha256hex;
        this.timestamp = timestamp;
    }

    @JsonProperty("item-hash")
    public String getSha256hex() {
        return "sha-256:" + sha256hex;
    }

    @JsonProperty("entry-timestamp")
    public String getTimestamp() {
        return ISODateFormatter.format(timestamp);
    }

    public static CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(Entry.class);
    }
}
