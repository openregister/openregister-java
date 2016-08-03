package uk.gov.register.functional.db;

public class TestRecord {
    public final String primaryKey;

    public final int entryNumber;

    public TestRecord(String primaryKey, int entryNumber) {
        this.primaryKey = primaryKey;
        this.entryNumber = entryNumber;
    }
    public String getPrimaryKey() {
        return primaryKey;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

}
