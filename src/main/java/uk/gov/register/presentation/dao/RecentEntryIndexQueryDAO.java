package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.skife.jdbi.v2.sqlobject.Bind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RecentEntryIndexQueryDAO {

    private final DB db;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RecentEntryIndexQueryDAO(DB db) {
        this.db = db;
    }

    public List<JsonNode> getLatestEntries(@Bind("limit") int maxNumberToFetch) {
        return db.select(
                "SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT ?",
                resultSet -> {
                    List<JsonNode> jsonNodes = new ArrayList<>();
                    while (resultSet.next()) {
                        jsonNodes.add(convertToJsonNode(resultSet.getBytes(1)));
                    }
                    return jsonNodes;
                },
                maxNumberToFetch
        );
    }

    public JsonNode findByKeyValue(String key, String value) {
        return db.select(
                "SELECT entry FROM ordered_entry_index WHERE entry @> ? ORDER BY id DESC limit 1",
                resultSet -> resultSet.next() ? convertToJsonNode(resultSet.getBytes(1)) : null,
                PGObjectFactory.jsonbObject(String.format("{\"entry\":{\"%s\": \"%s\"}}", key, value))
        );
    }

    public JsonNode findByHash(String hash) {
        return db.select(
                "SELECT entry FROM ordered_entry_index WHERE entry @> ? ORDER BY id DESC limit 1",
                resultSet -> resultSet.next() ? convertToJsonNode(resultSet.getBytes(1)) : null,
                PGObjectFactory.jsonbObject(String.format("{\"hash\":\"%s\"}", hash))
        );
    }

    private JsonNode convertToJsonNode(byte[] entry) {
        try {
            return objectMapper.readValue(entry, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}