package uk.gov.indexer.dao;

public class CurrentKey {
    public final int serial_number;
    public final String key;

    public CurrentKey(String key, int serial_number) {
        this.serial_number = serial_number;
        this.key = key;
    }

    @SuppressWarnings("unused, used by DAO")
    public int getSerial_number() {
        return serial_number;
    }

    @SuppressWarnings("unused, used by DAO")
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", serial_number, key);
    }
}
