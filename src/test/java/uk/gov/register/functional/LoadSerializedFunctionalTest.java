package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.db.*;
import uk.gov.register.util.HashValue;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class LoadSerializedFunctionalTest {
    private static final TestRegister testRegister = TestRegister.register;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static TestItemCommandDAO testItemDAO;
    private static TestEntryDAO testEntryDAO;
    private static TestRecordDAO testRecordDAO;
    private static String schema = testRegister.getSchema();

    @BeforeClass
    public static void setUp() throws Exception {
        Handle handle = register.handleFor(testRegister);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testRecordDAO = handle.attach(TestRecordDAO.class);
    }

    @Before
    public void setup() {
        register.wipe();
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf.tsv")));
        Response r = send(input);
        System.out.println(r.readEntity(String.class));
        assertThat(r.getStatus(), equalTo(200));

        TestDBItem expectedItem1 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"register\",\"phase\":\"alpha\",\"register\":\"register\",\"text\":\"A register name.\"}"));
        TestDBItem expectedItem2 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "243a2dafca693363f99c38487a03d1a241915c47a38ad5627ad941c9e52b4c7b"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"text\",\"phase\":\"alpha\",\"text\":\"Description of register entry.\"}"));
        TestDBItem expectedItem3 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"registry\",\"phase\":\"alpha\",\"text\":\"Body responsible for maintaining one or more registers\"}"));
        TestDBItem expectedItem4 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"phase\",\"phase\":\"alpha\",\"text\":\"Phase of a register or service as defined by the [digital service manual](https://www.gov.uk/service-manual).\"}"));
        TestDBItem expectedItem5 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "ecbbde36c6a9808b5f116c63f9ca14773ac3fac251b53e21a1d9fd4b2dd1b35c"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"copyright\",\"phase\":\"alpha\",\"text\":\"Copyright for the data in the register.\"}"));
        TestDBItem expectedItem6 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"),
                nodeOf("{\"cardinality\":\"n\",\"datatype\":\"string\",\"field\":\"fields\",\"phase\":\"alpha\",\"register\":\"field\",\"text\":\"Set of field names.\"}"));
        TestDBItem expectedItem7 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "2238e546c1d9e81a3715d10949dedced0311f596304fbf9bb48c50833f8ab025"),
                nodeOf("{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}"));
        TestDBItem expectedItem8 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test\"}"));
        TestDBItem expectedItem9 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test2\"}"));


        List<TestDBItem> storedItems = testItemDAO.getItems(schema);
        assertThat(storedItems, containsInAnyOrder(expectedItem1, expectedItem2, expectedItem3, expectedItem4, expectedItem5, expectedItem6, expectedItem7, expectedItem8, expectedItem9));

        List<Entry> entries = testEntryDAO.getAllEntries(schema);
        assertThat(entries.get(0).getEntryNumber(), is(1));
        assertThat(entries.get(0).getItemHashes().get(0).getValue(), is("955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0"));
        assertThat(entries.get(1).getEntryNumber(), is(2));
        assertThat(entries.get(1).getItemHashes().get(0).getValue(), is("243a2dafca693363f99c38487a03d1a241915c47a38ad5627ad941c9e52b4c7b"));
        assertThat(entries.get(2).getEntryNumber(), is(3));
        assertThat(entries.get(2).getItemHashes().get(0).getValue(), is("4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"));
        assertThat(entries.get(3).getEntryNumber(), is(4));
        assertThat(entries.get(3).getItemHashes().get(0).getValue(), is("1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"));
        assertThat(entries.get(4).getEntryNumber(), is(5));
        assertThat(entries.get(4).getItemHashes().get(0).getValue(), is("ecbbde36c6a9808b5f116c63f9ca14773ac3fac251b53e21a1d9fd4b2dd1b35c"));
        assertThat(entries.get(5).getEntryNumber(), is(6));
        assertThat(entries.get(5).getItemHashes().get(0).getValue(), is("61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"));
        assertThat(entries.get(6).getEntryNumber(), is(7));
        assertThat(entries.get(6).getItemHashes().get(0).getValue(), is("2238e546c1d9e81a3715d10949dedced0311f596304fbf9bb48c50833f8ab025"));
        assertThat(entries.get(7).getEntryNumber(), is(8));
        assertThat(entries.get(7).getItemHashes().get(0).getValue(), is("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));
        assertThat(entries.get(8).getEntryNumber(), is(9));
        assertThat(entries.get(8).getItemHashes().get(0).getValue(), is("b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"));

        TestRecord record1 = testRecordDAO.getRecord("ft_openregister_test", schema);
        assertThat(record1.getEntryNumber(), equalTo(8));
        assertThat(record1.getPrimaryKey(), equalTo("ft_openregister_test"));
        TestRecord record2 = testRecordDAO.getRecord("ft_openregister_test2", schema);
        assertThat(record2.getEntryNumber(), equalTo(9));
        assertThat(record2.getPrimaryKey(), equalTo("ft_openregister_test2"));
    }

    @Test
    public void shouldReturnBadRequestWhenNotValidRsf() {
        String entry = "foo bar";
        Response response = send(entry);

        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"RSF parsing error\",\"details\":\"String is empty or is in incorrect format\"}"));
    }

    @Test
    public void shouldReturnBadRequestForOrphanItems() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-orphan-rsf.tsv")));
        Response response = send(input);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Orphan add item (line:16): sha-256:d00d4b610e9b5af160a7e5e836eec9e12626cac61823eda1c3ec9a59a78eefaa\"}"));
    }

    @Test
    public void shouldReturnBadRequestForNonCanonicalItems() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-non-canonical-item.tsv")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"RSF parsing error\",\"details\":\"Non canonical JSON: { \\\"register\\\":\\\"ft_openregister_test\\\",   \\\"text\\\":\\\"SomeText\\\" }\"}"));
    }

    @Test
    public void shouldRollbackIfCheckedRootHashDoesNotMatchExpectedOne() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf-invalid-root-hash.tsv")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(testItemDAO.getItems(schema), empty());
        assertThat(testEntryDAO.getAllEntries(schema), empty());
    }

    @Test
    public void shouldUploadMultiItemEntries() throws IOException {
        System.setProperty("multi-item-entries-enabled", "true");
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-by-registry.rsf")));
        Response r = send(input);
        assertThat(r.getStatus(), equalTo(200));

    }

    @After
    public void clearMultiItemsEnabled() {
        System.clearProperty("multi-item-entries-enabled");
    }

    private Response send(String payload) {
        return register.loadRsf(testRegister, RsfRegisterDefinition.REGISTER_REGISTER + payload);
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, JsonNode.class);
    }
}
