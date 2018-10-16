package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.jackson.Jackson;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;
import uk.gov.register.core.Blob;
import uk.gov.register.core.Entry;
import uk.gov.register.core.EntryType;
import uk.gov.register.core.Record;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.db.TestDBBlob;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestBlobCommandDAO;
import uk.gov.register.serialization.RegisterResult;
import uk.gov.register.util.CanonicalJsonMapper;

import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.client.Entity.entity;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.views.representations.ExtraMediaType.APPLICATION_RSF_TYPE;

public class DataUploadFunctionalTest {
    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static String schema;

    private final CanonicalJsonMapper canonicalJsonMapper = new CanonicalJsonMapper();
    private static IndexQueryDAO testRecordDAO;
    private static TestEntryDAO testEntryDAO;
    private static TestBlobCommandDAO testItemDAO;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Handle handle = register.handleFor(TestRegister.register);
        testRecordDAO = handle.attach(IndexQueryDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testItemDAO = handle.attach(TestBlobCommandDAO.class);
        schema = TestRegister.register.getSchema();
    }

    @Before
    public void setUp() throws Exception {
        register.wipe();
        register.loadRsf(TestRegister.register, RsfRegisterDefinition.REGISTER_REGISTER);
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() {
        JsonNode inputItem = canonicalJsonMapper.readFromBytes("{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}".getBytes());
        String rsf = "add-item\t{\"register\":\"ft_openregister_test\",\"text\":\"SomeText\"}\n" +
                "append-entry\tuser\tft_openregister_test\t2018-07-26T15:05:16Z\tsha-256:3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254";

        Response r = register.loadRsf(TestRegister.register, rsf);
        assertThat(r.getStatus(), equalTo(200));

        TestDBBlob storedItem = testItemDAO.getItems(schema).get(7);
        assertThat(storedItem.contents, equalTo(inputItem));
        assertThat(storedItem.hashValue, equalTo(Blob.blobHash(inputItem)));

        Entry entry = testEntryDAO.getAllEntries(schema).get(0);
        assertThat(entry, equalTo(new Entry(1, storedItem.hashValue, entry.getTimestamp(), "ft_openregister_test", EntryType.user)));

        Record record = testRecordDAO.findRecords(Arrays.asList("ft_openregister_test"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record.getEntry().getKey(), equalTo("ft_openregister_test"));

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
        String rsf = "add-item\t{\"phase\":\"alpha\",\"register\":\"register1\",\"text\":\"Register1 Text\"}\n" +
                "add-item\t{\"phase\":\"alpha\",\"register\":\"register2\",\"text\":\"Register2 Text\"}\n" +
                "append-entry\tuser\tregister1\t2018-07-26T15:30:06Z\tsha-256:98d89fd39d305a7ffb409b24714e921e56b3365565860598133e77cd46b48996\n" +
                "append-entry\tuser\tregister2\t2018-07-26T15:30:06Z\tsha-256:fbc0134b8945ac09551ad7afded161a71c5ab01f2687bfc93b55bd27d985a2b1";

        Response r = register.loadRsf(TestRegister.register, rsf);

        assertThat(r.getStatus(), equalTo(200));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes("{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}".getBytes());
        JsonNode canonicalItem2 = canonicalJsonMapper.readFromBytes("{\"register\":\"register2\",\"text\":\"Register2 Text\", \"phase\":\"alpha\"}".getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);
        Instant timestamp = entries.get(0).getTimestamp();
        assertThat(entries,
                contains(
                        new Entry(1, Blob.blobHash(canonicalItem1), timestamp, "register1", EntryType.user),
                        new Entry(2, Blob.blobHash(canonicalItem2), timestamp, "register2", EntryType.user)
                )
        );

        List<TestDBBlob> items = testItemDAO.getItems(schema);
        assertThat(items,
                IsCollectionContaining.hasItems(
                        new TestDBBlob(canonicalItem1),
                        new TestDBBlob(canonicalItem2)
                )
        );

        Record record1 = testRecordDAO.findRecords(Arrays.asList("register1"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record1.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record1.getEntry().getKey(), equalTo("register1"));
        Record record2 = testRecordDAO.findRecords(Arrays.asList("register2"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record2.getEntry().getEntryNumber(), equalTo(2));
        assertThat(record2.getEntry().getKey(), equalTo("register2"));
    }

    @Test
    public void loadTwoSameItems_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";

        Response r = register.loadRsf(TestRegister.register,
                "add-item\t{\"phase\":\"alpha\",\"register\":\"register1\",\"text\":\"Register1 Text\"}\n" +
                "add-item\t{\"phase\":\"alpha\",\"register\":\"register1\",\"text\":\"Register1 Text\"}\n" +
                "append-entry\tuser\tregister1\t2017-06-23T11:32:15Z\tsha-256:98d89fd39d305a7ffb409b24714e921e56b3365565860598133e77cd46b48996\n" +
                "append-entry\tuser\tregister2\t2017-06-23T11:32:15Z\tsha-256:98d89fd39d305a7ffb409b24714e921e56b3365565860598133e77cd46b48996");

        assertThat(r.getStatus(), equalTo(200));

        JsonNode canonicalItem = canonicalJsonMapper.readFromBytes(item1.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);

        assertThat(entries,
                contains(
                        new Entry(1, Blob.blobHash(canonicalItem), entries.get(0).getTimestamp(), "register1", EntryType.user),
                        new Entry(2, Blob.blobHash(canonicalItem), entries.get(1).getTimestamp(), "register2", EntryType.user)
                )
        );

        List<TestDBBlob> items = testItemDAO.getItems(schema);
        assertThat(items,
                IsCollectionContaining.hasItem(
                        new TestDBBlob(canonicalItem)
                )
        );

        Record record = testRecordDAO.findRecords(Arrays.asList("register1"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record.getEntry().getKey(), equalTo("register1"));
    }

    @Test
    public void loadTwoNewItems_withOneItemPreexistsInDatabase_addsTwoRowsInEntryAndOnlyOneRowInItemTable() {
        String item1 = "{\"register\":\"register1\",\"text\":\"Register1 Text\", \"phase\":\"alpha\"}";
        Response r = register.loadRsf(TestRegister.register,
                "add-item\t{\"phase\":\"alpha\",\"register\":\"register1\",\"text\":\"Register1 Text\"}\n" +
                        "append-entry\tuser\tregister1\t2017-06-23T11:42:34Z\tsha-256:98d89fd39d305a7ffb409b24714e921e56b3365565860598133e77cd46b48996");

        assertThat(r.getStatus(), equalTo(200));


        r = register.loadRsf(TestRegister.register,
                "add-item\t{\"phase\":\"alpha\",\"register\":\"register1\",\"text\":\"Register1 Text\"}\n" +
                        "append-entry\tuser\tregister2\t2017-06-23T11:42:34Z\tsha-256:98d89fd39d305a7ffb409b24714e921e56b3365565860598133e77cd46b48996");

        assertThat(r.getStatus(), equalTo(200));

        JsonNode canonicalItem1 = canonicalJsonMapper.readFromBytes(item1.getBytes());

        List<Entry> entries = testEntryDAO.getAllEntries(schema);
        Instant timestamp = entries.get(0).getTimestamp();
        assertThat(entries,
                contains(
                        new Entry(1, Blob.blobHash(canonicalItem1), timestamp, "register1", EntryType.user),
                        new Entry(2, Blob.blobHash(canonicalItem1), timestamp, "register2", EntryType.user)
                )
        );

        List<TestDBBlob> items = testItemDAO.getItems(schema);
        assertThat(items,
                IsCollectionContaining.hasItem(
                        new TestDBBlob(canonicalItem1)
                )
        );

        Record record1 = testRecordDAO.findRecords(Arrays.asList("register1"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record1.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record1.getEntry().getKey(), equalTo("register1"));
        Record record2 = testRecordDAO.findRecords(Arrays.asList("register2"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record2.getEntry().getEntryNumber(), equalTo(2));
        assertThat(record2.getEntry().getKey(), equalTo("register2"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenMissingKeyField() {
        String rsf = "add-item\t{}\n" +
                "append-entry\tuser\tfoo\t2018-07-26T13:53:34Z\tsha-256:44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a";

        Response response = register.loadRsf(TestRegister.register, rsf);
        assertThat(response.getStatus(), equalTo(400));

        RegisterResult result = response.readEntity(RegisterResult.class);
        assertThat(result.getMessage(), equalTo("Failed to load RSF"));
        assertThat(result.getDetails(), containsString("Entry does not contain primary key field 'register'"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenBlankKeyField() {
        String rsf = "add-item\t{\"register\":\"  \"}\n" +
                "append-entry\tuser\t  \t2018-07-26T13:58:25Z\tsha-256:adeec959e9f6a1481f1bd77d541f19c8e430b668c174e96bfe8ad5a224e3e6ee";

        Response response = register.loadRsf(TestRegister.register, rsf);
        assertThat(response.getStatus(), equalTo(400));

        RegisterResult result = response.readEntity(RegisterResult.class);

        assertThat(result.getMessage(), equalTo("Failed to load RSF"));
        assertThat(result.getDetails(), containsString("Primary key field 'register' must have a valid value"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenEntryContainsInvalidFields() {
        String rsf = "add-item\t{\"foo\":\"bar\",\"register\":\"invalid-items\"}\n" +
                "append-entry\tuser\tinvalid-items\t2018-07-27T12:37:31Z\tsha-256:baeecd3c54f412d77089f25d6ba1637b1861b545a9b6b2eaceb757d328007e7c";

        Response response = register.loadRsf(TestRegister.register, rsf);
        assertThat(response.getStatus(), equalTo(400));

        RegisterResult result = response.readEntity(RegisterResult.class);
        assertThat(result.getMessage(), equalTo("Failed to load RSF"));
        assertThat(result.getDetails(), containsString("Entry contains invalid fields: [foo]"));
    }

    @Test
    public void validation_FailsToLoadEntryWhenFieldWithCardinalityManyIsNotAJsonArray() {
        String rsf = "add-item\t{\"fields\":\"single-field\",\"register\":\"some-register\"}\n" +
                "append-entry\tuser\tsome-register\t2018-07-27T12:39:04Z\tsha-256:8a4545d97667542075a7d245c4274f6cc73dac3083c55a6aee81fe1b911b7d91";

        Response response = register.loadRsf(TestRegister.register, rsf);
        assertThat(response.getStatus(), equalTo(400));

        RegisterResult result = response.readEntity(RegisterResult.class);
        assertThat(result.getMessage(), equalTo("Failed to load RSF"));
        assertThat(result.getDetails(), containsString("Field 'fields' has cardinality 'n'"));
    }

    @Test
    public void requestWithoutCredentials_isRejectedAsUnauthorized() throws Exception {
        // register.target() is unauthenticated
        Response response = register.target(TestRegister.register).path("/load-rsf")
                .request()
                .post(entity("add-item\t{}", APPLICATION_RSF_TYPE));
        assertThat(response.getStatus(), equalTo(401));
    }
}
