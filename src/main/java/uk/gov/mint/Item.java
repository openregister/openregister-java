package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;
import org.postgresql.util.PGobject;
import uk.gov.register.util.CanonicalJsonMapper;

import java.sql.SQLException;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private final String sha256hex;
    private final JsonNode content;

    public Item(JsonNode content) {
        this.content = content;
        this.sha256hex = itemHash(content);
    }

    public static String itemHash(JsonNode content) {
        return DigestUtils.sha256Hex(canonicalJsonMapper.writeToBytes(content));
    }

    public String getSha256hex() {
        return sha256hex;
    }

    @SuppressWarnings("unused, used by DAO")
    public PGobject getContent() throws SQLException {
        PGobject data = new PGobject();
        data.setType("jsonb");
        data.setValue(content.toString());
        return data;
    }

    public String getKey(String key) {
        return content.get(key).textValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return sha256hex.equals(item.sha256hex);

    }

    @Override
    public int hashCode() {
        return sha256hex.hashCode();
    }
}
