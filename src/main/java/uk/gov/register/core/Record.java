package uk.gov.register.core;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.Iterator;

public class Record {
    public final Entry entry;
    public final Item item;

    public Record(Entry entry, Item item) {
        this.entry = entry;
        this.item = item;
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema entrySchema = Entry.csvSchema();
        CsvSchema.Builder schemaBuilder = entrySchema.rebuild();

        for (Iterator<CsvSchema.Column> iterator = Item.csvSchema(fields).rebuild().getColumns(); iterator.hasNext();) {
            schemaBuilder.addColumn(iterator.next().getName(), CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }
}
