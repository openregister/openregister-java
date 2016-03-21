package uk.gov.indexer.dao;

import java.sql.Timestamp;

public class Entry {

    private final int entryNumber;
    private final String itemHash;
    private final Timestamp timestamp;

    public Entry(int entryNumber, String itemHash, Timestamp timestamp) {
        this.entryNumber = entryNumber;
        this.itemHash = itemHash;
        this.timestamp = timestamp;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public String getItemHash() {
        return itemHash;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
