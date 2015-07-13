package uk.gov.register.presentation.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.skife.jdbi.v2.sqlobject.Bind;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RecentEntryIndexQueryDAO {

    private final DataSource dataSource;
    private ObjectMapper objectMapper = new ObjectMapper();

    public RecentEntryIndexQueryDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<JsonNode> getLatestEntries(@Bind("limit") int maxNumberToFetch) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT entry FROM ordered_entry_index ORDER BY id DESC LIMIT ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, maxNumberToFetch);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<JsonNode> jsonNodes = new ArrayList<>();
                while (resultSet.next()) {
                    jsonNodes.add(convertToJsonNode(resultSet.getBytes(1)));
                }
                return jsonNodes;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public JsonNode findByKeyValue(String key, String value) {
        return find(PGObjectFactory.jsonbObject(String.format("{\"entry\":{\"%s\": \"%s\"}}", key, value)));
    }

    public JsonNode findByHash(String hash) {
        return find(PGObjectFactory.jsonbObject(String.format("{\"hash\":\"%s\"}", hash)));
    }

    private JsonNode find(PGobject pGobject) {
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT entry FROM ordered_entry_index WHERE entry @> ? ORDER BY id DESC limit 1";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setObject(1, pGobject);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return convertToJsonNode(resultSet.getBytes(1));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode convertToJsonNode(byte[] entry) {
        try {
            return objectMapper.readValue(entry, JsonNode.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}