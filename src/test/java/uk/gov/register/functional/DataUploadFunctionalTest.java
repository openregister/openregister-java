package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.Jackson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Item;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.db.*;
import uk.gov.register.util.CanonicalJsonMapper;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.client.Entity.entity;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class DataUploadFunctionalTest {
    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static String schema;

    private final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private static TestRecordDAO testRecordDAO;
    private static TestEntryDAO testEntryDAO;
    private static TestItemCommandDAO testItemDAO;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Handle handle = register.handleFor(TestRegister.register);
        testRecordDAO = handle.attach(TestRecordDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        schema = TestRegister.register.getSchema();
    }

    @Before
    public void setUp() throws Exception {
        register.wipe();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        JsonNode inputItem = canonicalJsonMapper.readFromBytes("{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}".getBytes());
        Response r = register.mintLines(TestRegister.register, inputItem.toString());
        assertThat(r.getStatus(), equalTo(204));

        TestDBItem storedItem = testItemDAO.getItems(schema).get(0);
        assertThat(storedItem.contents, equalTo(inputItem));
        assertThat(storedItem.hashValue, equalTo(Item.itemHash(inputItem)));

        Entry entry = testEntryDAO.getAllEntries(schema).get(0);
        assertThat(entry, equalTo(new Entry(1, storedItem.hashValue, entry.getTimestamp(), "ft_openregister_test", EntryType.user)));

        TestRecord record = testRecordDAO.getRecord("ft_openregister_test", schema);
        assertThat(record.getEntryNumber(), equalTo(1));
        assertThat(record.getPrimaryKey(), equalTo("ft_openregister_test"));

        Response response = register.getRequest(TestRegister.register, "/record/ft_openregister_test.json");

        assertThat(response.getStatus(), equalTo(200));
        Map actualJson = Jackson.newObjectMapper().convertValue(response.readEntity(JsonNode.class).get("ft_openregister_test"), Map.class);
        actualJson.remove("entry-timestamp"); // ignore the timestamp as we can't do exact match
        assertThat(actualJson.get("entry-number"), is("1"));
        List<Map<String,Object>> itemMaps = (List<Map<String,Object>>)actualJson.get("item");
        assertThat(itemMaps.size(), is(1));
        Map<String, Object> itemMap = itemMaps.get(0);
        assertThat(itemMap.get("register"), is("ft_openregister_test"));
        assertThat(itemMap.get("text"), is("SomeText"));
    }

    @Test
    public void loadTwoDistinctItems_addsTwoRowsInEntryAndItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        Response r = register.mintLines(TestRegister.register, item1, item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes(item1.getBytes());
        JsonNode canonicalItem2 = canonicalJsonMapper.readFromBytes(item2.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);
        Instant timestamp = entries.get(0).getTimestamp();
        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem1), timestamp, "register1", EntryType.user),
                        new Entry(2, Item.itemHash(canonicalItem2), timestamp, "register2", EntryType.user)
                )
        );

        List<TestDBItem> items = testItemDAO.getItems(schema);
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem1),
                        new TestDBItem(canonicalItem2)
                )
        );

        TestRecord record1 = testRecordDAO.getRecord("register1", schema);
        assertThat(record1.getEntryNumber(), equalTo(1));
        assertThat(record1.getPrimaryKey(), equalTo("register1"));
        TestRecord record2 = testRecordDAO.getRecord("register2", schema);
        assertThat(record2.getEntryNumber(), equalTo(2));
        assertThat(record2.getPrimaryKey(), equalTo("register2"));

    }

    @Test
    public void loadTwoSameItems_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        String item2 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";

        Response r = register.mintLines(TestRegister.register,item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem = canonicalJsonMapper.readFromBytes(item1.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);

        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem), entries.get(0).getTimestamp(), "register1", EntryType.user),
                        new Entry(2, Item.itemHash(canonicalItem), entries.get(1).getTimestamp(), "register1", EntryType.user)
                )
        );

        List<TestDBItem> items = testItemDAO.getItems(schema);
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem)
                )
        );


        TestRecord record = testRecordDAO.getRecord("register1", schema);
        assertThat(record.getEntryNumber(), equalTo(2));
        assertThat(record.getPrimaryKey(), equalTo("register1"));
    }

    @Test
    public void loadTwoNewItems_withOneItemPreexistsInDatabase_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        Response r = register.mintLines(TestRegister.register, item1);
        assertThat(r.getStatus(), equalTo(204));

        String item2 = "{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}";

        r = register.mintLines(TestRegister.register, item1 + "\n" + item2);

        assertThat(r.getStatus(), equalTo(204));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes(item1.getBytes());
        JsonNode canonicalItem2 = canonicalJsonMapper.readFromBytes(item2.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);
        Instant timestamp = entries.get(0).getTimestamp();
        assertThat(entries,
                contains(
                        new Entry(1, Item.itemHash(canonicalItem1), timestamp, "register1", EntryType.user),
                        new Entry(2, Item.itemHash(canonicalItem1), timestamp, "register1", EntryType.user),
                        new Entry(3, Item.itemHash(canonicalItem2), timestamp, "register2", EntryType.user)
                )
        );

        List<TestDBItem> items = testItemDAO.getItems(schema);
        assertThat(items,
                contains(
                        new TestDBItem(canonicalItem1),
                        new TestDBItem(canonicalItem2)
                )
        );


        TestRecord record1 = testRecordDAO.getRecord("register1", schema);
        assertThat(record1.getEntryNumber(), equalTo(2));
        assertThat(record1.getPrimaryKey(), equalTo("register1"));
        TestRecord record2 = testRecordDAO.getRecord("register2", schema);
        assertThat(record2.getEntryNumber(), equalTo(3));
        assertThat(record2.getPrimaryKey(), equalTo("register2"));

    }

    @Test
    public void validation_FailsToLoadEntryWhenNonEmptyPrimaryKeyFieldIsNotExist() {
        Response response = register.mintLines(TestRegister.register, "{}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Item did not contain key field. Error entry: '{}'"));

        response = register.mintLines(TestRegister.register, "{\"register\":\"  \"}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Primary key field 'register' must have a valid value. Error entry: '{\"register\":\"  \"}'"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenEntryContainsInvalidFields() {
        Response response = register.mintLines(TestRegister.register, "{\"foo\":\"bar\",\"foo1\":\"bar1\"}");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Item did not contain key field. Error entry: '{\"foo\":\"bar\",\"foo1\":\"bar1\"}'"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenFieldWithCardinalityManyIsNotAJsonArray() {
        String entry = "{\"register\":\"someregister\",\"fields\":\"value\"}";
        Response response = register.mintLines(TestRegister.register, entry);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("Field 'fields' has cardinality 'n' so the value must be an array of 'string'. Error entry: '" + entry + "'"));
    }

    @Test
    public void requestWithoutCredentials_isRejectedAsUnauthorized() throws Exception {
        // register.target() is unauthenticated
        Response response = register.target(TestRegister.register).path("/load")
                .request()
                .post(entity("{}", APPLICATION_JSON_TYPE));
        assertThat(response.getStatus(), equalTo(401));
    }
}
