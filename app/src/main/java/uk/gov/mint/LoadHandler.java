package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import uk.gov.store.EntriesUpdateDAO;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private final EntryValidator entryValidator;

    public LoadHandler(EntriesUpdateDAO entriesUpdateDAO, EntryValidator entryValidator) {
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.entryValidator = entryValidator;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
        entriesUpdateDAO.ensureTableExists();
    }

    public void handle(String registerName, String payload) {
        processEntries(registerName, payload.split("\n"));
    }

    private void processEntries(String registerName, String[] entries) {
        final List<byte[]> entriesAsBytes = Arrays.stream(entries)
                .map(e -> {
                    try {
                        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                        entryValidator.validateEntry(registerName, jsonNode);
                        return canonicalJsonMapper.writeToBytes(hashedEntry(jsonNode));
                    } catch (Exception ex) {
                        throw Throwables.propagate(ex);
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

