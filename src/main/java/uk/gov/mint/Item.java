package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.digest.DigestUtils;

import java.nio.charset.StandardCharsets;

public class Item {
    private static final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private final String sha256hex;
    private final byte[] canonicalContent;

    public Item(byte[] content) {
        this.canonicalContent = canonicalJson(content);
        this.sha256hex = DigestUtils.sha256Hex(canonicalContent);
    }

    public Item(JsonNode content) {
        this(content.toString().getBytes(StandardCharsets.UTF_8));
    }

    private byte[] canonicalJson(byte[] content) {
        return canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(content));
    }

    public String getSha256hex() {
        return sha256hex;
    }

    public byte[] getCanonicalContent() {
        return canonicalContent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return !(sha256hex != null ? !sha256hex.equals(item.sha256hex) : item.sha256hex != null);

    }

    @Override
    public int hashCode() {
        return sha256hex != null ? sha256hex.hashCode() : 0;
    }
}
