package uk.gov.indexer.dao;

public class Record {
    public final Entry entry;
    public final Item item;

    public Record(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
    }
}
