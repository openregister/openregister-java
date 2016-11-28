package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.util.PGobject;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.HashValue;

import java.sql.SQLException;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    private final JsonNode content;
    private final HashValue hashValue;

    public Item(JsonNode content) {
        this(itemHash(content), content);
    }

    public Item(HashValue hashValue, JsonNode content) {
        this.hashValue = hashValue;
        this.content = content;
    }

    public static HashValue itemHash(JsonNode content) {
        String hash = DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));

        return new HashValue(HashingAlgorithm.SHA256, hash);
    }

    public HashValue getSha256hex() {
        return hashValue;
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

    public String getValue(String key) {
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

        if (hashValue != null ? !hashValue.equals(item.hashValue) : item.hashValue != null) return false;
        return content != null ? content.equals(item.content) : item.content == null;

    }

    @Override
    public int hashCode() {
        int result = hashValue != null ? hashValue.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{content=" + content.toString() + "}";
    }
}
