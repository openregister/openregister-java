package uk.gov.register.core;

public class Record {
    private final Entry entry;
    private final Item item;

    public Record(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
    }

    public Entry getEntry() {
        return entry;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        return entry.equals(record.entry);
    }

    @Override
    public int hashCode() {
        int result = entry.hashCode();
        return result;
    }
}
