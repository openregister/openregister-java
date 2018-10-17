package uk.gov.register.core;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Entry {
    private final int indexEntryNumber;
    private final int entryNumber;
    private final List<HashValue> hashValues;
    private final Instant timestamp;
    private final EntryType entryType;
    private String key;

    public Entry(int entryNumber, HashValue hashValue, Instant timestamp, String key, EntryType entryType) {
        this.entryNumber = entryNumber;
        this.indexEntryNumber = entryNumber;
        this.hashValues = new ArrayList<>(Arrays.asList(hashValue));
        this.timestamp = timestamp;
        this.key = key;
        this.entryType = entryType;
    }

    public Entry(int entryNumber, List<HashValue> hashValues, Instant timestamp, String key, EntryType entryType) {
        this(entryNumber, entryNumber, hashValues, timestamp, key, entryType);
    }

    public Entry(int indexEntryNumber, int entryNumber, List<HashValue> hashValues, Instant timestamp, String key, EntryType entryType) {
        this.indexEntryNumber = indexEntryNumber;
        this.entryNumber = entryNumber;
        this.hashValues = hashValues;
        this.timestamp = timestamp;
        this.key = key;
        this.entryType = entryType;
    }

    public Entry(int indexEntryNumber, int entryNumber,
                       List<HashValue> hashValues, Instant timestamp,
                       String key) {
        this.indexEntryNumber = indexEntryNumber;
        this.entryNumber = entryNumber;
        this.hashValues = hashValues;
        this.timestamp = timestamp;
        this.key = key;
        this.entryType = EntryType.system;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<HashValue> getBlobHashes() {
        return hashValues;
    }

    public long getTimestampAsLong() {
        return timestamp.getEpochSecond();
    }

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public Integer getIndexEntryNumber() {
        return indexEntryNumber;
    }

    public String getTimestampAsISOFormat() {
        return ISODateFormatter.format(timestamp);
    }

    public String getKey() {
        return key;
    }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Entry entry = (Entry) o;

        if (indexEntryNumber != entry.indexEntryNumber) return false;
        if (entryNumber != entry.entryNumber) return false;
        if (key != null ? !key.equals(entry.key) : entry.key != null) return false;
        if (timestamp != null ? !timestamp.equals(entry.timestamp) : entry.timestamp != null) return false;
        return hashValues == null ? entry.hashValues == null : CollectionUtils.isEqualCollection(hashValues, entry.hashValues);
    }

    @Override
    public int hashCode() {
        int result = 0;

        Iterator<HashValue> iterator = hashValues.iterator();
        while (iterator.hasNext()) {
            HashValue hashValue = iterator.next();
            result += hashValue.hashCode();
        }

        result = 31 * result + indexEntryNumber;
        result = 31 * result + entryNumber;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);

        return result;
    }
}
