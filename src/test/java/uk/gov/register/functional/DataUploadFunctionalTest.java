package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
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
import uk.gov.register.core.Item;
import uk.gov.register.functional.app.CleanDatabaseRule;
import uk.gov.register.functional.db.TestDBItem;
import uk.gov.register.functional.db.TestRecord;
import uk.gov.register.core.Entry;
import uk.gov.register.RegisterApplication;
import uk.gov.register.RegisterConfiguration;
import uk.gov.register.util.CanonicalJsonMapper;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.db.TestDBSupport.*;

public class DataUploadFunctionalTest {
    public static final int APPLICATION_PORT = 9000;

    private final DropwizardAppRule<RegisterConfiguration> appRule = new DropwizardAppRule<>(RegisterApplication.class,
            ResourceHelpers.resourceFilePath("test-app-config.yaml"),
            ConfigOverride.config("jerseyClient.timeout", "3000ms"),
            ConfigOverride.config("register", "register"));

    @Rule
    public TestRule ruleChain = RuleChain.
            outerRule(new CleanDatabaseRule()).
            around(appRule);


    private final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();


    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        JsonNode inputItem = canonicalJsonMapper.readFromBytes("{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}".getBytes());
        Response r = send(inputItem.toString());
        assertThat(r.getStatus(), equalTo(204));

        TestDBItem storedItem = testItemDAO.getItems().get(0);
        assertThat(storedItem.contents, equalTo(inputItem));
        assertThat(storedItem.hashValue, equalTo(Item.itemHash(inputItem)));

        Entry entry = testEntryDAO.getAllEntries().get(0);
        assertThat(entry, equalTo(new Entry(1, storedItem.hashValue, Instant.now(), "ft_openregister_test")));

        TestRecord record = testRecordDAO.getRecord("ft_openregister_test");
        assertThat(record.getEntryNumber(), equalTo(1));
        assertThat(record.getPrimaryKey(), equalTo("ft_openregister_test"));

        Client client = testClient();
        Response response = client.target(String.format("http://localhost:%d/record/ft_openregister_test.json", appRule.getLocalPort()))
                .request().header("Host","address.test.register.gov.uk").get();

        assertThat(response.getStatus(), equalTo(200));
        Map actualJson = response.readEntity(Map.class);
        actualJson.remove("entry-timestamp"); // ignore the timestamp as we can't do exact match
        assertThat(actualJson, equalTo(ImmutableMap.of(
                "entry-number", "1",
                "item-hash", storedItem.hashValue.toString(),
                "register", "ft_openregister_test",
                "text", "SomeText",
                "key", "ft_openregister_test"
        )));
    }

    @Test
    public void loadTwoDistinctItems_addsTwoRowsInEntryAndItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        Response r = send(item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes(item1.getBytes());
        JsonNode canonicalItem2 = canonicalJsonMapper.readFromBytes(item2.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries();
        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem1), Instant.now(), "register1"),
                        new Entry(2, Item.itemHash(canonicalItem2), Instant.now(), "register2")
                )
        );

        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem1),
                        new TestDBItem(canonicalItem2)
                )
        );

        TestRecord record1 = testRecordDAO.getRecord("register1");
        assertThat(record1.getEntryNumber(), equalTo(1));
        assertThat(record1.getPrimaryKey(), equalTo("register1"));
        TestRecord record2 = testRecordDAO.getRecord("register2");
        assertThat(record2.getEntryNumber(), equalTo(2));
        assertThat(record2.getPrimaryKey(), equalTo("register2"));

    }

    @Test
    public void loadTwoSameItems_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";

        Response r = send(item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem = canonicalJsonMapper.readFromBytes(item1.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries();
        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem), Instant.now(), "register1"),
                        new Entry(2, Item.itemHash(canonicalItem), Instant.now(), "register2")
                )
        );

        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem)
                )
        );


        TestRecord record = testRecordDAO.getRecord("register1");
        assertThat(record.getEntryNumber(), equalTo(2));
        assertThat(record.getPrimaryKey(), equalTo("register1"));
    }

    @Test
    public void loadTwoNewItems_withOneItemPreexistsInDatabase_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        Response r = send(item1);
        assertThat(r.getStatus(), equalTo(204));

        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        r = send(item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes(item1.getBytes());
        JsonNode canonicalItem2 = canonicalJsonMapper.readFromBytes(item2.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries();
        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem1), Instant.now(), "register1"),
                        new Entry(2, Item.itemHash(canonicalItem1), Instant.now(), "register1"),
                        new Entry(3, Item.itemHash(canonicalItem2), Instant.now(), "register2")
                )
        );

        List<TestDBItem> items = testItemDAO.getItems();
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem1),
                        new TestDBItem(canonicalItem2)
                )
        );


        TestRecord record1 = testRecordDAO.getRecord("register1");
        assertThat(record1.getEntryNumber(), equalTo(2));
        assertThat(record1.getPrimaryKey(), equalTo("register1"));
        TestRecord record2 = testRecordDAO.getRecord("register2");
        assertThat(record2.getEntryNumber(), equalTo(3));
        assertThat(record2.getPrimaryKey(), equalTo("register2"));

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

    @Test
    public void requestWithoutCredentials_isRejectedAsUnauthorized() throws Exception {
        Response response = testClient().target("http://localhost:" + APPLICATION_PORT + "/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json("{}"));
        assertThat(response.getStatus(), equalTo(401));
    }

    private Response send(String... payload) {
        return authenticatingClient().target("http://localhost:" + APPLICATION_PORT + "/load")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.json(String.join("\n", payload)));

    }

    private JerseyClient authenticatingClient() {
        ClientConfig configuration = new ClientConfig();
        configuration.register(HttpAuthenticationFeature.basic("foo", "bar"));
        return JerseyClientBuilder.createClient(configuration);
    }

    private Client testClient() {
        return new io.dropwizard.client.JerseyClientBuilder(appRule.getEnvironment())
                .using(appRule.getConfiguration().getJerseyClientConfiguration())
                .build("test client");
    }
}
