package uk.gov.register.db.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemMapper implements ResultSetMapper<Item> {
    private final ObjectMapper objectMapper;

    public ItemMapper() {
        objectMapper = Jackson.newObjectMapper();
    }

    @Override
    public Item map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        try {
            return new Item(
                    new HashValue(HashingAlgorithm.SHA256, r.getString("sha256hex")),
                    new HashValue(HashingAlgorithm.SHA256, r.getString("blob_hash")),
                    objectMapper.readValue(r.getString("content"), JsonNode.class)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
