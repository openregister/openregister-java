package uk.gov.indexer.dao;

import java.nio.charset.StandardCharsets;

public class FatEntry {
    public final int serial_number;
    public final byte[] contents;

    public FatEntry(int serial_number, byte[] contents) {
        this.serial_number = serial_number;
        this.contents = contents;
    }

    public OrderedEntryIndex dbEntry() {
        return new OrderedEntryIndex(serial_number, new String(contents, StandardCharsets.UTF_8));
    }
}
