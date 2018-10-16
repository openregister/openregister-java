package uk.gov.register.util;

public class EntryBlobPair {
    private final int entryNumber;
    private final HashValue sha256hex;

    public EntryBlobPair(int entryNumber, HashValue sha256hex) {
        this.entryNumber = entryNumber;
        this.sha256hex = sha256hex;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public String getSha256hex() {
        return sha256hex.getValue();
    }
}
