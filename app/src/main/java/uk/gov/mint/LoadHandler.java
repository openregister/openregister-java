package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.store.EntriesUpdateDAO;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final EntriesUpdateDAO entriesUpdateDAO;

    public LoadHandler(EntriesUpdateDAO entriesUpdateDAO) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
        entriesUpdateDAO.ensureTableExists();
    }

    public void handle(String payload) throws Exception {
        processEntries(payload.split("\n"));
    }

    private void processEntries(String[] entries) throws Exception {
        final List<byte[]> entriesAsBytes = Arrays.stream(entries)
                .map(e -> {
                    try {
                        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes("UTF-8"));
                        return canonicalJsonMapper.writeToBytes(hashedEntry(jsonNode));
                    } catch (Exception e1) {
                        throw new RuntimeException("Error parsing json entry: " + e, e1);
                    }
                })
                .collect(Collectors.toList());
        entriesUpdateDAO.add(entriesAsBytes);
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}
