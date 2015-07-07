package uk.gov.register.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonNodeMapper implements ResultSetMapper<JsonNode> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        byte[] entry = r.getBytes("entry");
        try {
            return objectMapper.readValue(entry, JsonNode.class);
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }
}
