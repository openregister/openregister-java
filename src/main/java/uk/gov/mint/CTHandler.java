package uk.gov.mint;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.setup.Environment;
import uk.gov.MintConfiguration;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

public class CTHandler implements Loader {
    private final String ctserver;
    private final Client client;
    private final CanonicalJsonMapper canonicalJsonMapper;

    public CTHandler(MintConfiguration configuration, Environment environment, final String appName) {
        this.ctserver = configuration.ctServer().get();

        this.client = new JerseyClientBuilder(environment)
                .using(configuration.getJerseyClientConfiguration())
                .build(appName);

        this.canonicalJsonMapper = new CanonicalJsonMapper();
    }

    @Override
    public void load(List<JsonNode> entries) {
        WebTarget wt = client.target(ctserver);

        entries.forEach(singleEntry -> {
            Response response = wt.request()
                    .post(Entity.entity(singleEntry, MediaType.APPLICATION_JSON), Response.class);
            try {
                if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                    throw new CTException(response.readEntity(String.class));
                }
            } finally {
                response.close();
            }
        });
    }
}
