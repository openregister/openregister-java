package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item {
    public final String sha256hex;
    public final JsonNode content;

    public Item(String sha256hex, JsonNode content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }

    public String getSha256hex() {
        return "sha-256:" + sha256hex;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        if (sha256hex != null ? !sha256hex.equals(item.sha256hex) : item.sha256hex != null) return false;
        return content != null ? content.equals(item.content) : item.content == null;

    }

    @Override
    public int hashCode() {
        int result = sha256hex != null ? sha256hex.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
