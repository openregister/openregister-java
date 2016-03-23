package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Item {

    private final String sha256hex;
    private final byte[] content;

    public Item(String sha256hex, byte[] content) {
        this.sha256hex = sha256hex;
        this.content = content;
    }

    public String getSha256hex() {
        return sha256hex;
    }

    public byte[] getContent() {
        return content;
    }

    @SuppressWarnings("unused, used by DAO")
    public PGobject getJsonContent() {
        return pgObject(new String(content, StandardCharsets.UTF_8));
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

    private PGobject pgObject(String itemContent) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(itemContent);
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
