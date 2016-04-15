package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import io.dropwizard.jackson.Jackson;

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

    public ObjectNode json() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        ObjectNode jsonNodes = objectMapper.convertValue(entry, ObjectNode.class);
        jsonNodes.setAll((ObjectNode) item.content.deepCopy());
        return jsonNodes;
    }
}
