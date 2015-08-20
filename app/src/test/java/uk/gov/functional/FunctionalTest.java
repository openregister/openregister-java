package uk.gov.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.MintApplication;
import uk.gov.mint.CanonicalJsonMapper;
import uk.gov.store.EntriesQueryDAO;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.*;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;


public class FunctionalTest {
    private static String postgresConnectionString = "jdbc:postgresql://localhost:5432/ft_mint";
    public static TestKafkaCluster testKafkaCluster;

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(
                    new ExternalResource() {
                        @Override
                        protected void before() throws Throwable {
                            testKafkaCluster = new TestKafkaCluster(6000);
                        }

                        @Override
                        protected void after() {
                            try {
                                testKafkaCluster.stop();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
            ).
            around(
                    new CleanDatabaseRule(postgresConnectionString)
            ).
            around(
                    new DropwizardAppRule<>(MintApplication.class,
                            ResourceHelpers.resourceFilePath("test-config.yaml"),
                            ConfigOverride.config("database.url", postgresConnectionString))
            );


    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        String messageString = String.format("{\"name\":\"ft_mint_test\",\"test\":\"test_%s\"}",
                String.valueOf(System.currentTimeMillis()));
        byte[] message = messageString.getBytes();
        JsonNode messageJson = canonicalJsonMapper.readFromBytes(message);

        waitForMessageToBeConsumed();

        send(Collections.singletonList(messageString));

        waitForMessageToBeConsumed();

        byte[] actual = tableRecord();
        String actualString = new String(actual);
        final JsonNode actualJson = canonicalJsonMapper.readFromBytes(actual);

        JsonNode entryNode = actualJson.get("entry");
        assertThat(actualJson.get("hash"), notNullValue(JsonNode.class));
        assertThat(entryNode, notNullValue(JsonNode.class));

        assertThat(entryNode.get("name").textValue(),
                equalTo(messageJson.get("name").textValue()));
        assertThat(entryNode.get("test").textValue(),
                equalTo(messageJson.get("test").textValue()));

        List<String> messages = testKafkaCluster.readMessages("register", 1);
        assertThat(messages, is(Collections.singletonList(actualString)));
    }

    private void send(List<String> payload) {
        try {
            JerseyClient jerseyClient = JerseyClientBuilder.createClient();
            Response response = jerseyClient.target("http://localhost:4568/load").request(MediaType.APPLICATION_JSON_TYPE).buildPost(Entity.json(String.join("\n", payload))).invoke();
            if (response.getStatus() != 200)
                System.err.println("Unexpected result: " + response.readEntity(String.class));
            else
                System.out.println("Loaded " + payload.size() + " entries...");
        } catch (Exception e) {
            System.err.println("Error occurred sending data to mint: " + e);
        }
    }

    private byte[] tableRecord() throws SQLException {
        Connection pgConnection = DriverManager.getConnection(postgresConnectionString, "postgres", "");
        try (Statement statement = pgConnection.createStatement()) {
            statement.execute("SELECT ENTRY FROM " + EntriesQueryDAO.tableName);
            ResultSet resultSet = statement.getResultSet();
            return resultSet.next() ? resultSet.getBytes(1) : null;
        }
    }

    private void waitForMessageToBeConsumed() throws InterruptedException {
        Thread.sleep(3000);
    }
}
