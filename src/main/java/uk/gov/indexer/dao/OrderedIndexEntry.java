package uk.gov.indexer.dao;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.postgresql.util.PGobject;

import java.sql.SQLException;

public class OrderedIndexEntry {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public final int serial_number;
    public final PGobject content;
    public final String primaryKey;

    public OrderedIndexEntry(int serial_number, String registerName, String content) {
        this.serial_number = serial_number;
        this.content = pgObject(content);
        this.primaryKey = getKey(registerName, content);
    }

    private String getKey(String registerName, String entry) {
        JsonNode jsonNode = getJsonNode(entry);
        return jsonNode.get("entry").get(registerName).getTextValue();
    }

    private JsonNode getJsonNode(String entry) {
        try {
            return objectMapper.readTree(entry);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
