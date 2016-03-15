package uk.gov.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import uk.gov.MintApplication;
import uk.gov.functional.db.TestDBItem;
import uk.gov.mint.CanonicalJsonMapper;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.functional.TestDBSupport.*;

public class PGFunctionalTest {
    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(
                    new CleanDatabaseRule()
            ).
            around(
                    new DropwizardAppRule<>(MintApplication.class,
                            ResourceHelpers.resourceFilePath("test-config.yaml"),
                            ConfigOverride.config("database.url", postgresConnectionString))
            );


    private final JerseyClient jerseyClient = authenticatingClient();

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();

        String inputItem = "{\"register\":\"ft_mint_test\",\"text\":\"SomeText\"}";
        Response r = send(inputItem);
        assertThat(r.getStatus(), equalTo(204));

        JsonNode storedEntry = canonicalJsonMapper.readFromBytes(testEntriesDAO.getEntry());

        assertThat(storedEntry.get("hash"), notNullValue(JsonNode.class));

        JsonNode entryNode = storedEntry.get("entry");
        assertThat(entryNode, notNullValue(JsonNode.class));

        assertThat(entryNode.get("register").textValue(), equalTo("ft_mint_test"));
        assertThat(entryNode.get("text").textValue(), equalTo("SomeText"));


        TestDBItem storedItem = testItemDAO.getItem();
        assertThat(storedItem.contents, equalTo(inputItem.getBytes()));
        assertThat(storedItem.sha256hex, equalTo(DigestUtils.sha256Hex(inputItem.getBytes())));

        String hex = testEntryDAO.getHex();
        assertThat(hex, equalTo(storedItem.sha256hex));
    }

    @Test
    public void validation_FailsToLoadEntryWhenNonEmptyPrimaryKeyFieldIsNotExist() {
        Response response = send("{}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Entry does not contain primary key field 'register'. Error entry: '{}'"));

        response = send("{\"register\":\"  \"}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Primary key field 'register' must have a valid value. Error entry: '{\"register\":\"  \"}'"));
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
