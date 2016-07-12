package uk.gov.organisation.client;

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import java.util.Optional;

public class GovukOrganisationClient {
    private final Client client;
    private final GovukClientConfiguration config;

    @Inject
    public GovukOrganisationClient(Client client, GovukClientConfiguration config) {
        this.client = client;
        this.config = config;
    }

    public Optional<GovukOrganisation> getOrganisation(String organisation) {
        try {
            return Optional.of(client
                    .target(config.getGovukEndpoint())
                    .path("/api/organisations/" + organisation)
                    .request()
                    .get(GovukOrganisation.class));
        } catch (WebApplicationException | ProcessingException e) {
            // ProcessingException is thrown on timeout
            return Optional.empty();
        }
    }
}
