package uk.gov.register.presentation;

public class DbEntry {
    private final int serialNumber;
    private final DbContent dbContent;

    public DbEntry(int serialNumber, DbContent dbContent) {
        this.serialNumber = serialNumber;
        this.dbContent = dbContent;
    }

    public DbContent getContent() {
        return dbContent;
    }

    public int getSerialNumber() {
        return serialNumber;
    }
}

