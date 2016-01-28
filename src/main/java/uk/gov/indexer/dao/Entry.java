package uk.gov.indexer.dao;

import java.nio.charset.StandardCharsets;

public class Entry {
    public final int serial_number;
    public final byte[] contents;
    private String leafInput;

    public Entry(int serial_number, byte[] contents, String leafInput) {
        this.serial_number = serial_number;
        this.contents = contents;
        this.leafInput = leafInput;
    }

    public OrderedEntryIndex dbEntry() {
        return new OrderedEntryIndex(serial_number, new String(contents, StandardCharsets.UTF_8), leafInput);
    }
}
