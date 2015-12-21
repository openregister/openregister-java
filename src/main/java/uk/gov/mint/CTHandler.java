package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import uk.gov.store.EntriesUpdateDAO;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CTHandler implements Handler {
    private final CanonicalJsonMapper canonicalJsonMapper;
    private final String register;
    private final String ctserver;
    private final Client client;
    private final EntryValidator entryValidator;

    public CTHandler(String register, String ctserver, Client client, EntryValidator entryValidator) {
        this.register = register;
        this.ctserver = ctserver;
        this.client = client;
        this.entryValidator = entryValidator;
        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void handle(String payload) {
        processEntries(payload.split("\n"));
    }

    private void processEntries(String[] entries) {
        if(client != null && StringUtils.isNotBlank(ctserver)) {
            WebTarget wt = client.target(ctserver);

            Arrays.stream(entries).forEach(e -> {
                try {
                    final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                    entryValidator.validateEntry(register, jsonNode);
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
}
