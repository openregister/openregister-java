package uk.gov.register.core;

public class FatEntry {
    public final Entry entry;
    public final Item item;

    public FatEntry(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
    }
}
