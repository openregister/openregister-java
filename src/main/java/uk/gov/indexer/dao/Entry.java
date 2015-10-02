package uk.gov.indexer.dao;

import java.nio.charset.StandardCharsets;

public class Entry {
    public final int serial_number;
    public final byte[] contents;

    public Entry(int serial_number, byte[] contents) {
        this.serial_number = serial_number;
        this.contents = contents;
    }

    public OrderedIndexEntry dbEntry(String registerName) {
        return new OrderedIndexEntry(serial_number, registerName, new String(contents, StandardCharsets.UTF_8));
    }
}
