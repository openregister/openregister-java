package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.TimeZone;

public class Entry {
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    static {
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.of("UTC")));
    }

    @JsonProperty("entry-number")
    public final String entryNumber;

    private final String sha256hex;

    private final Timestamp timestamp;

    public Entry(String entryNumber, String sha256hex, Timestamp timestamp) {
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
        return simpleDateFormat.format(timestamp);
    }

    public static CsvSchema csvSchema() {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY);
        return csvMapper.schemaFor(Entry.class);
    }
}
