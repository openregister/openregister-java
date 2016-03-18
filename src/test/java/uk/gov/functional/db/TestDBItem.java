package uk.gov.functional.db;

public class TestDBItem {
    public final String sha256hex;
    public final byte[] contents;

    public TestDBItem(String sha256hex, byte[] contents) {
        this.sha256hex = sha256hex;
        this.contents = contents;
    }
}
