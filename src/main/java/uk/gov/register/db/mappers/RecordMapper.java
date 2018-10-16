package uk.gov.register.db.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import uk.gov.register.core.Blob;
import uk.gov.register.core.HashingAlgorithm;
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
        List<Blob> blobs = new ArrayList<>();

        String[] blobHashes = (String[]) r.getArray("sha256hex").getArray();
        String[] blobContent = (String[]) r.getArray("content").getArray();

        if (blobHashes.length != blobContent.length) {
            throw new RuntimeException("Number of blob hashes not equal to number of blob content");
        }

        for (int i = 0; i < blobHashes.length; i++) {
            try {
                blobs.add(new Blob(new HashValue(HashingAlgorithm.SHA256, blobHashes[i]), objectMapper.readValue(blobContent[i], JsonNode.class)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new Record(
                entryMapper.map(index, r, ctx),
                blobs
        );
    }
}
