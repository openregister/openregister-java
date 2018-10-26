package uk.gov.register.core;

import uk.gov.register.util.HashValue;
import uk.gov.register.util.ISODateFormatter;

import java.time.Instant;

public class Entry {
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

    public Instant getTimestamp() {
        return timestamp;
    }

    public HashValue getItemHash() {
        return hashValue;
    }

    public long getTimestampAsLong() {
        return timestamp.getEpochSecond();
    }

    public Integer getEntryNumber() {
        return entryNumber;
    }

    public Integer getIndexEntryNumber() {
        return entryNumber;
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
