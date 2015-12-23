package uk.gov.functional;

import com.fasterxml.jackson.databind.JsonNode;
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
import uk.gov.mint.CanonicalJsonMapper;
import uk.gov.store.EntriesUpdateDAO;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class FunctionalTest {
    private static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint";
    private static String ctserver = "http://localhost:8089/add-json";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8089);

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
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        stubFor(post(urlEqualTo("/add-json"))
                .willReturn(aResponse()
                        .withStatus(200)
                ));

        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        Response r =  send("{\"register\":\"ft_mint_test\",\"text\":\"SomeText\"}");
        assertThat(r.getStatus(), equalTo(200));

        JsonNode storedEntry = canonicalJsonMapper.readFromBytes(tableRecord());

        assertThat(storedEntry.get("hash"), notNullValue(JsonNode.class));

        JsonNode entryNode = storedEntry.get("entry");
        assertThat(entryNode, notNullValue(JsonNode.class));

        assertThat(entryNode.get("register").textValue(), equalTo("ft_mint_test"));
        assertThat(entryNode.get("text").textValue(), equalTo("SomeText"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenNonEmptyPrimaryKeyFieldIsNotExist() {
        Response response = send("{}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Entry does not contain primary key field 'register'. Error entry: '{}'"));

        response = send("{\"register\":\"  \"}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Primary key field 'register' must have some valid value. Error entry: '{\"register\":\"  \"}'"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenEntryContainsInvalidFields() {
        Response response = send("{\"foo\":\"bar\",\"foo1\":\"bar1\"}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Entry contains invalid fields: [foo, foo1]. Error entry: '{\"foo\":\"bar\",\"foo1\":\"bar1\"}'"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenFieldWithCardinalityManyIsNotAJsonArray() {
        String entry = "{\"register\":\"someregister\",\"fields\":\"value\"}";
        Response response = send(entry);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Field 'fields' has cardinality 'n' so the value must be an array of 'string'. Error entry: '" + entry + "'"));
    }

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

    private byte[] tableRecord() throws SQLException {
        Connection pgConnection = DriverManager.getConnection(postgresConnectionString, "postgres", "");
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT ENTRY FROM " + EntriesUpdateDAO.tableName);
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }
}
