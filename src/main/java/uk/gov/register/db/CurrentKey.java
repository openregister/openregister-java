package uk.gov.register.db;

public class CurrentKey {
    public final int entry_number;
    public final String key;

    public CurrentKey(String key, int entry_number) {
        this.entry_number = entry_number;
        this.key = key;
    }

    @SuppressWarnings("unused, used by DAO")
    public int getEntry_number() {
        return entry_number;
    }

    @SuppressWarnings("unused, used by DAO")
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return String.format("{%s,%s}", entry_number, key);
    }
}
