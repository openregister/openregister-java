package uk.gov.mint;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.store.DataStore;
import uk.gov.store.LogStream;

public class LoadHandler {
    private final DataStore dataStore;
    private final LogStream logStream;
    private final CanonicalJsonMapper canonicalJsonMapper;

    public LoadHandler(DataStore dataStore, LogStream logStream) {

        this.dataStore = dataStore;
        this.logStream = logStream;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void handle(String payload) throws Exception {
        byte[] payloadBytes = payload.getBytes();
        JsonNode jsonNode;
        try {
            jsonNode = canonicalJsonMapper.readFromBytes(payloadBytes);
            dataStore.add(canonicalJsonMapper.writeToBytes(jsonNode));
        } catch (JsonProcessingException e) {
            throw new Exception("Error parsing JSON payload.", e);
        }
    }
}
