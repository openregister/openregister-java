package uk.gov.functional;

import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


public class FunctionalTest {
    private static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint";

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(
                    new CleanDatabaseRule(postgresConnectionString)
            ).
            around(
                    new DropwizardAppRule<>(MintApplication.class,
                            ResourceHelpers.resourceFilePath("test-config.yaml"),
                            ConfigOverride.config("database.url", postgresConnectionString))
            );


    //TODO: rewrite this test
    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        String messageString = String.format("{\"register\":\"ft_mint_test\",\"text\":\"test_%s\"}",
                String.valueOf(System.currentTimeMillis()));
        byte[] message = messageString.getBytes();
        JsonNode messageJson = canonicalJsonMapper.readFromBytes(message);

        send(Collections.singletonList(messageString));

        byte[] actual = tableRecord();
        final JsonNode actualJson = canonicalJsonMapper.readFromBytes(actual);

        JsonNode entryNode = actualJson.get("entry");
        assertThat(actualJson.get("hash"), notNullValue(JsonNode.class));
        assertThat(entryNode, notNullValue(JsonNode.class));

        assertThat(entryNode.get("register").textValue(),
                equalTo(messageJson.get("register").textValue()));
        assertThat(entryNode.get("text").textValue(),
                equalTo(messageJson.get("text").textValue()));
    }

    private void send(List<String> payload) {
        JerseyClient jerseyClient = authenticatingClient();
        Response response = jerseyClient.target("http://localhost:4568/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));
        if (response.getStatus() != 204) {
            throw new RuntimeException("Unexpected result: " + response.readEntity(String.class));
        }
        System.out.println("Loaded " + payload.size() + " entries...");
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
