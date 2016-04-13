package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item {
    private final String sha256hex;
    public final JsonNode content;

    public Item(String sha256hex, JsonNode content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }

    public Stream<Map.Entry<String, JsonNode>> getFieldsStream() {
        return StreamSupport.stream(((Iterable<Map.Entry<String, JsonNode>>) content::fields).spliterator(), false);
    }

    public static CsvSchema csvSchema(Iterable<String> fields) {
        CsvSchema.Builder schemaBuilder = new CsvSchema.Builder();
        for (String value : fields) {
            schemaBuilder.addColumn(value, CsvSchema.ColumnType.STRING);
        }
        return schemaBuilder.build();
    }
}
