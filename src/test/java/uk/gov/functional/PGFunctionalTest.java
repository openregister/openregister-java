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
import java.util.List;

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
    private final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();


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


        TestDBItem storedItem = testItemDAO.getItems().get(0);
        assertThat(storedItem.contents, equalTo(inputItem.getBytes()));
        assertThat(storedItem.sha256hex, equalTo(DigestUtils.sha256Hex(inputItem.getBytes())));

        String hex = testEntryDAO.getAllHex().get(0);
        assertThat(hex, equalTo(storedItem.sha256hex));
    }

    @Test
    public void loadTwoDistinctItems_addsTwoRowsInEntryAndItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        Response r = send(item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        String canonicalItem1 = new String(canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(item1.getBytes())));
        String canonicalItem2 = new String(canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(item2.getBytes())));

        List<String> entries = testEntryDAO.getAllHex();
        assertThat(entries.size(), equalTo(2));
        assertThat(entries.get(0), equalTo(DigestUtils.sha256Hex(canonicalItem1)));
        assertThat(entries.get(1), equalTo(DigestUtils.sha256Hex(canonicalItem2)));

        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items.size(), equalTo(2));
        assertThat(items.get(0).sha256hex, equalTo(DigestUtils.sha256Hex(canonicalItem1)));
        assertThat(items.get(1).sha256hex, equalTo(DigestUtils.sha256Hex(canonicalItem2)));

        assertThat(items.get(0).contents, equalTo(canonicalItem1.getBytes()));
        assertThat(items.get(1).contents, equalTo(canonicalItem2.getBytes()));

    }

    @Test
    public void loadTwoSameItems_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";

        Response r = send(item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        String canonicalItem = new String(canonicalJsonMapper.writeToBytes(canonicalJsonMapper.readFromBytes(item1.getBytes())));

        List<String> entries = testEntryDAO.getAllHex();
        assertThat(entries.size(), equalTo(2));
        assertThat(entries.get(0), equalTo(DigestUtils.sha256Hex(canonicalItem)));
        assertThat(entries.get(1), equalTo(DigestUtils.sha256Hex(canonicalItem)));

        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items.size(), equalTo(1));
        assertThat(items.get(0).sha256hex, equalTo(DigestUtils.sha256Hex(canonicalItem)));

        assertThat(items.get(0).contents, equalTo(canonicalItem.getBytes()));
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
