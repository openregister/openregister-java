package uk.gov.register.core;

import uk.gov.mint.Item;

public class FatEntry {
    public final Entry entry;
    public final Item item;

    public FatEntry(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
    }
}
