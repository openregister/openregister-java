package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import uk.gov.store.EntriesUpdateDAO;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LoadHandler implements Handler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final String register;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private final EntryValidator entryValidator;

    public LoadHandler(String register, EntriesUpdateDAO entriesUpdateDAO, EntryValidator entryValidator) {
        this.register = register;
        this.entriesUpdateDAO = entriesUpdateDAO;
        this.entryValidator = entryValidator;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
        entriesUpdateDAO.ensureTableExists();
    }

    public void handle(String payload) {
        processEntries(payload.split("\n"));
    }

    private void processEntries(String[] entries) {
        final List<byte[]> entriesAsBytes = Arrays.stream(entries)
                .map(e -> {
                    try {
                        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                        entryValidator.validateEntry(register, jsonNode);
                        return canonicalJsonMapper.writeToBytes(hashedEntry(jsonNode));
                    } catch (Exception ex) {
                        return ExceptionUtils.rethrow(ex);
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
