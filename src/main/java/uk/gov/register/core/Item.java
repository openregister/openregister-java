package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.util.PGobject;
import uk.gov.register.util.CanonicalJsonMapper;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    private final String sha256hex;
    private final JsonNode content;

    public Item(JsonNode content) {
        this(itemHash(content), content);
    }

    public Item(String sha256hex, JsonNode content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }

    public static String itemHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }

    public String getItemHash() {
        return "sha-256:" + sha256hex;
    }

    public String getSha256hex() {
        return sha256hex;
    }

    public JsonNode getContent() {
        return content;
    }

    @SuppressWarnings("unused, used by DAO")
    public PGobject getContentAsJsonb() throws SQLException {
        PGobject data = new PGobject();
        data.setType("jsonb");
        data.setValue(content.toString());
        return data;
    }

    public String getKey(String key) {
        return content.get(key).textValue();
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

    @Override
    public String toString() {
        return "Item{content=" + content.toString() + "}";
    }
}
