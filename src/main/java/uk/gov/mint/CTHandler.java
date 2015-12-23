package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.MintConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CTHandler implements Handler {
    private final Logger LOGGER = LoggerFactory.getLogger(CTHandler.class);

    private final String register;
    private final String ctserver;
    private final Client client;
    private final EntryValidator entryValidator;
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CTHandler(MintConfiguration configuration, Environment environment, final String appName, EntryValidator entryValidator) {
        LOGGER.info("CTHandler created");

        this.register = configuration.getRegister();
        this.ctserver = configuration.getCTServer();
        this.entryValidator = entryValidator;

        this.client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build(appName);

        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    public void handle(String payload) {
        processEntries(payload.split("\n"));
    }

    private void processEntries(String[] entries) {
        LOGGER.info("Sending entries to CT server");

        WebTarget wt = client.target(ctserver);

        Arrays.stream(entries).forEach(e -> {
                    try {
                        final JsonNode jsonNode = canonicalJsonMapper.readFromBytes(e.getBytes(StandardCharsets.UTF_8));
                        entryValidator.validateEntry(register, jsonNode);
                        Response r = wt.request()
                                .post(Entity.entity(jsonNode, MediaType.APPLICATION_JSON), Response.class);
                        if (r.getStatusInfo() != Response.Status.OK) {
                            throw new CTException(r.getStatus(), r.readEntity(String.class));
                        }
                    } catch (IOException ex) {
                        ExceptionUtils.rethrow(ex);
                    }
                }
        );
    }
}
