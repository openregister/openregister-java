package uk.gov.indexer.dao;

public class Entry {
    public final int serial_number;
    public final byte[] contents;

    public Entry(int serial_number, byte[] contents) {
        this.serial_number = serial_number;
        this.contents = contents;
    }
}
