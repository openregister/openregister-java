package uk.gov.indexer.dao;

public class CurrentKey {
    private final int serial_number;
    private final String key;

    public CurrentKey(int serial_number, String key) {
        this.serial_number = serial_number;
        this.key = key;
    }

    @SuppressWarnings("unused, used from DAO")
    public int getSerial_number() {
        return serial_number;
    }

    @SuppressWarnings("unused, used from DAO")
    public String getKey() {
        return key;
    }
}
