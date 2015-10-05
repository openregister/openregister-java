package uk.gov.indexer.dao;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.databind.JsonNode;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class OrderedIndexEntry {
    public final int serial_number;
    public final PGobject content;
    public final String primaryKey;

    public OrderedIndexEntry(int serial_number, String registerName, String content) {
        this.serial_number = serial_number;
        this.content = pgObject(content);
        this.primaryKey = getKey(registerName, content);
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = Jackson.jsonNodeOf(entry);
        return jsonNode.get("entry").get(registerName).textValue();
    }

    private PGobject pgObject(String entry) {
        try {
            PGobject pGobject = new PGobject();
            pGobject.setType("jsonb");
            pGobject.setValue(entry);
            return pGobject;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
