package uk.gov.indexer.dao;

import java.sql.Timestamp;

public class Entry {

    private final int entryNumber;
    private final String itemHash;
    private final Timestamp timestamp;

    Entry(int entryNumber, String itemHash, Timestamp timestamp) {
        this.entryNumber = entryNumber;
        this.itemHash = itemHash;
        this.timestamp = timestamp;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    @SuppressWarnings("unused, used by DAO")
    public String getItemHash() {
        return itemHash;
    }

    @SuppressWarnings("unused, used by DAO")
    public Timestamp getTimestamp() {
        return timestamp;
    }
}
