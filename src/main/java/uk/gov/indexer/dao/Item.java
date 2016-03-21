package uk.gov.indexer.dao;

public class Item {

    private final String itemHash;
    private final byte[] content;

    public Item(String itemHash, byte[] content) {
        this.itemHash = itemHash;
        this.content = content;
    }

    public String getItemHash() {
        return itemHash;
    }

    public byte[] getContent() {
        return content;
    }
}
