package uk.gov.register.presentation.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class JsonNodeMapper implements ResultSetMapper<JsonNode> {
    @Override
    public JsonNode map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        return JsonObjectMapper.convertToJsonNode(r.getBytes("entry"));
    }
}
