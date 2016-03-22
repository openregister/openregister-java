package uk.gov.indexer.dao;

import org.postgresql.util.PGobject;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Item {

    private final String itemHash;
    private final byte[] content;

    public Item(String itemHash, byte[] content) {
        this.itemHash = itemHash;
        this.content = content;
    }

    public String getItemHash() {
        return itemHash;
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

        return !(itemHash != null ? !itemHash.equals(item.itemHash) : item.itemHash != null);
    }

    @Override
    public int hashCode() {
        return itemHash != null ? itemHash.hashCode() : 0;
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
