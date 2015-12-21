package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CTHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(CTHandler.class);

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
                        throw new CTException(r.getStatus(), r.readEntity(String.class));
                    }
                }
                catch(IOException ex) {
                    ExceptionUtils.rethrow(ex);
                }
            }
            );
        }
    }
}
