package uk.gov.register.core;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.*;

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

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema entrySchema = Entry.csvSchemaWithOmittedFields(Arrays.asList("item-hash"));
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

        return entry.equals(record.entry);
    }

    @Override
    public int hashCode() {
        int result = entry.hashCode();
        return result;
    }
}
