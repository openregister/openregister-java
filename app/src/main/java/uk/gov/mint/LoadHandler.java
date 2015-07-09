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
        for (String entry : payload.split("\n"))
            processEntry(entry);
    }

    private void processEntry(String entry) throws Exception {
        byte[] payloadBytes = entry.getBytes();
        JsonNode jsonNode;
        try {
            jsonNode = canonicalJsonMapper.readFromBytes(payloadBytes);
            dataStore.add(canonicalJsonMapper.writeToBytes(jsonNode));
            logStream.notifyOfNewEntries();
        } catch (JsonProcessingException e) {
            throw new Exception("Error parsing JSON entry [" + entry + "]", e);
        }
    }

    public void shutdown() throws Exception {
        dataStore.close();
        logStream.close();
    }
}
