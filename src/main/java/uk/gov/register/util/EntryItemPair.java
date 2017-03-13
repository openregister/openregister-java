package uk.gov.register.util;

public class EntryItemPair {
    private final int entryNumber;
    private final HashValue sha256hex;

    public EntryItemPair(int entryNumber, HashValue sha256hex) {
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
