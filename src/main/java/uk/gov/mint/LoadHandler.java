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

public class LoadHandler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final String register;
    private final String ctserver;
    private final Client client;
    private final EntriesUpdateDAO entriesUpdateDAO;
    private final EntryValidator entryValidator;

    public LoadHandler(String register, String ctserver, Client client, EntriesUpdateDAO entriesUpdateDAO, EntryValidator entryValidator) {
        this.register = register;
        this.ctserver = ctserver;
        this.client = client;
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
                        //Note: commented the entry validation till the data is not cleaned
                        //Also Validation doesn't respect the cardinality of a field. trello card https://trello.com/c/6GIewuwc
//                        entryValidator.validateEntry(register, jsonNode);
                        return canonicalJsonMapper.writeToBytes(hashedEntry(jsonNode));
                    } catch (Exception ex) {
                        return ExceptionUtils.rethrow(ex);
                    }
                })
                .collect(Collectors.toList());
        entriesUpdateDAO.add(entriesAsBytes);

        // Propagate to CT server if set
        if(client != null && StringUtils.isNotBlank(ctserver)) {
            WebTarget wt = client.target(ctserver);

            Arrays.stream(entries).forEach(e -> {
                try {
                    final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                    Response r = wt.request()
                            .post(Entity.entity(jsonNode, MediaType.APPLICATION_JSON), Response.class);
                    if(r.getStatusInfo() != Response.Status.OK) {
                        throw new RuntimeException(r.readEntity(String.class));
                    }
                }
                catch(Exception ex) {
                    ExceptionUtils.rethrow(ex);
                }
            }
            );
        }
    }

    private ObjectNode hashedEntry(JsonNode entryJsonNode) {
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
        jsonNode.put("hash", Digest.shasum(entryJsonNode.toString()));
        jsonNode.set("entry", entryJsonNode);
        return jsonNode;
    }

}
