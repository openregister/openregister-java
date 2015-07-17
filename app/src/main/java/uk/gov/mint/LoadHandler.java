package uk.gov.mint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.gov.store.EntriesUpdateDAO;
import uk.gov.store.LogStream;

public class LoadHandler {
    private final LogStream logStream;
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final EntriesUpdateDAO entriesUpdateDAO;

    public LoadHandler(EntriesUpdateDAO entriesUpdateDAO, LogStream logStream) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.logStream = logStream;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
        entriesUpdateDAO.ensureTableExists();
    }

    public void handle(String payload) throws Exception {
        for (String entry : payload.split("\n"))
            processEntry(entry);
    }

    private void processEntry(String entry) throws Exception {
        try {
            JsonNode entryJsonNode = canonicalJsonMapper.readFromBytes(entry.getBytes());
            entriesUpdateDAO.add(canonicalJsonMapper.writeToBytes(hashedEntry(entryJsonNode)));
            logStream.notifyOfNewEntries();
        } catch (JsonProcessingException e) {
            throw new Exception("Error parsing JSON entry [" + entry + "]", e);
        }
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

    public void shutdown() throws Exception {
        logStream.close();
    }
}
