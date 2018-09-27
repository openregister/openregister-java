package uk.gov.register.db.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Item;
import uk.gov.register.core.Record;
import uk.gov.register.util.HashValue;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecordMapper implements ResultSetMapper<Record> {
    private final EntryMapper entryMapper;
    private final ObjectMapper objectMapper;

    public RecordMapper() {
        this.entryMapper = new EntryMapper();
        objectMapper = Jackson.newObjectMapper();
    }

    @Override
    public Record map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        List<Item> items = new ArrayList<>();

        String itemHash = r.getString("sha256hex");
        String itemContent = r.getString("content");

        try {
            items.add(new Item(new HashValue(HashingAlgorithm.SHA256, itemHash), objectMapper.readValue(itemContent, JsonNode.class)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Record(
                entryMapper.map(index, r, ctx),
                items
        );
    }
}
