package uk.gov.register.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.util.PGobject;
import uk.gov.register.util.CanonicalJsonMapper;
import uk.gov.register.util.HashValue;
import uk.gov.register.util.JsonToBlobHash;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

    private final JsonNode content;
    private final HashValue v1HashValue;
    private final HashValue blobHash;
    private final Optional<Integer> itemOrder;

    public Item(JsonNode content) {
        this(itemHash(content), JsonToBlobHash.apply(content), content);
    }

    public Item(HashValue v1HashValue, HashValue blobHash, JsonNode content) {
        this.v1HashValue = v1HashValue;
        this.blobHash = blobHash;
        this.content = content;
        this.itemOrder = Optional.empty();
    }

    public Item(HashValue v1HashValue, HashValue blobHash, JsonNode content, Optional<Integer> itemOrder) {
        this.v1HashValue = v1HashValue;
        this.blobHash = blobHash;
        this.content = content;
        this.itemOrder = itemOrder;
    }

    public static HashValue itemHash(JsonNode content) {
        String hash = DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));

        return new HashValue(HashingAlgorithm.SHA256, hash);
    }

    public HashValue getSha256hex() {
        return v1HashValue;
    }

    public HashValue getBlobHash() {
        return blobHash;
    }

    public JsonNode getContent() {
        return content;
    }

    /*
     * Value may not be available. Used for pagination.
     */
    public Optional<Integer> getItemOrder() {
        return this.itemOrder;
    }

    @SuppressWarnings("unused, used by DAO")
    public PGobject getContentAsJsonb() throws SQLException {
        PGobject data = new PGobject();
        data.setType("jsonb");
        data.setValue(content.toString());
        return data;
    }

    public Optional<String> getValue(String key) {
        JsonNode value = content.get(key);
        return Optional.ofNullable(value).map(JsonNode::textValue);
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

        if (blobHash != null ? !blobHash.equals(item.blobHash) : item.blobHash != null) return false;
        return content != null ? content.equals(item.content) : item.content == null;

    }

    @Override
    public int hashCode() {
        return blobHash != null ? blobHash.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Item{" +
                "content=" + content +
                ", blobHash=" + blobHash +
                ", v1HashValue=" + v1HashValue +
                '}';
    }
}
