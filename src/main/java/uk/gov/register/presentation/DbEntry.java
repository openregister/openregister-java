package uk.gov.register.presentation;

public class DbEntry {
    private final int serialNumber;
    private final DbContent dbContent;
    private final String leaf_input;

    public DbEntry(int serialNumber, DbContent dbContent, String leaf_input) {
        this.serialNumber = serialNumber;
        this.dbContent = dbContent;
        this.leaf_input = leaf_input;
    }

    public DbContent getContent() {
        return dbContent;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public String getLeaf_input() {
        return leaf_input;
    }
}

