package uk.gov.register.core;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import uk.gov.register.util.HashValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Record {
    private final Entry entry;
    private final Map<HashValue, Item> items = new HashMap<>();

    public Record(Entry entry, Item item) {
        this.entry = entry;
        this.items.put(item.getSha256hex(), item);
    }

    public Record(Entry entry, Iterable<Item> items) {
        this.entry = entry;
        items.forEach(i -> this.items.put(i.getSha256hex(), i));
    }

    public Entry getEntry() {
        return entry;
    }

    public Map<HashValue, Item> getItems() {
        return items;
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema entrySchema = Entry.csvSchemaWithOmittedFields(Arrays.asList("key"));
        CsvSchema.Builder schemaBuilder = entrySchema.rebuild();

        for (Iterator<CsvSchema.Column> iterator = Item.csvSchema(fields).rebuild().getColumns(); iterator.hasNext();) {
            schemaBuilder.addColumn(iterator.next().getName(), CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Record record = (Record) o;

        if (!entry.equals(record.entry)) return false;
        return items.equals(record.items);
    }

    @Override
    public int hashCode() {
        int result = entry.hashCode();
        result = 31 * result + items.hashCode();
        return result;
    }
}
