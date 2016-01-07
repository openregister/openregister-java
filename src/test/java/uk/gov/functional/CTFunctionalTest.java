package uk.gov.functional;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.MintApplication;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class CTFunctionalTest {
    private static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint";
    private static String ctserver = "http://localhost:8090/add-json";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8090);

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(
                    new CleanDatabaseRule(postgresConnectionString)
            ).
            around(
                    new DropwizardAppRule<>(MintApplication.class,
                            ResourceHelpers.resourceFilePath("test-config.yaml"),
                            ConfigOverride.config("database.url", postgresConnectionString),
                            ConfigOverride.config("ctserver", ctserver))
            );


    private final JerseyClient jerseyClient = authenticatingClient();

    @Test
    public void checkThatErrorsFromCTServerArePropogatedBack() throws Exception {
        stubFor(post(urlEqualTo("/add-json"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Unable to parse JSON")
                ));

        Response r =  send("{\"register\":\"ft_mint_test\",\"text\":\"SomeText\"}");
        assertThat(r.getStatus(), equalTo(400));
    }

    private Response send(String... payload) {
        return jerseyClient.target("http://localhost:4568/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));

    }

    private JerseyClient authenticatingClient() {
        ClientConfig configuration = new ClientConfig();
        configuration.register(HttpAuthenticationFeature.basic("foo", "bar"));
        return JerseyClientBuilder.createClient(configuration);
    }
}
