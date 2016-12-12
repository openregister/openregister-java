package uk.gov.register.core;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.Arrays;
import java.util.Iterator;

public class Record {
    public final Entry entry;
    public final Item item;

    public Record(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
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
        return item.equals(record.item);
    }

    @Override
    public int hashCode() {
        int result = entry.hashCode();
        result = 31 * result + item.hashCode();
        return result;
    }
}
