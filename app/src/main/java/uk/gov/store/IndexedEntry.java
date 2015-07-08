package uk.gov.store;

public class IndexedEntry {
    private final byte[] entry;
    private final int serial;

    public IndexedEntry(int serial, byte[] entry) {
        this.entry = entry;
        this.serial = serial;
    }

    public byte[] getEntry() {
        return entry;
    }

    public int getSerial() {
        return serial;
    }
}
