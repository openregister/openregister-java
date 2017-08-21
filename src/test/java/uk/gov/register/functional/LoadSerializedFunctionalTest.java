package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.StringContains;
import org.junit.*;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.core.Record;
import uk.gov.register.db.IndexQueryDAO;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.db.*;
import uk.gov.register.util.HashValue;
import uk.gov.register.configuration.IndexFunctionConfiguration.IndexNames;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class LoadSerializedFunctionalTest {
    private static final TestRegister testRegister = TestRegister.register;

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static TestItemCommandDAO testItemDAO;
    private static TestEntryDAO testEntryDAO;
    private static IndexQueryDAO testRecordDAO;
    private static String schema = testRegister.getSchema();

    @BeforeClass
    public static void setUp() throws Exception {
        Handle handle = register.handleFor(testRegister);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testRecordDAO = handle.attach(IndexQueryDAO.class);
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
                new HashValue(HashingAlgorithm.SHA256, "ceae38992b310fba3ae77fd84e21cdb6838c90b36bcb558de02acd2f6589bd3f"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"text\",\"phase\":\"alpha\",\"text\":\"Description of register entry.\"}"));
        TestDBItem expectedItem3 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"registry\",\"phase\":\"alpha\",\"text\":\"Body responsible for maintaining one or more registers\"}"));
        TestDBItem expectedItem4 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"phase\",\"phase\":\"alpha\",\"text\":\"Phase of a register or service as defined by the [digital service manual](https://www.gov.uk/service-manual).\"}"));
        TestDBItem expectedItem5 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "c7e5a90c020f7686d9a275cb0cc164636745b10ae168a72538772692cc90d633"),
                nodeOf("{\"cardinality\":\"1\",\"datatype\":\"text\",\"field\":\"copyright\",\"phase\":\"alpha\",\"text\":\"Copyright for the data in the register.\"}"));
        TestDBItem expectedItem6 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"),
                nodeOf("{\"cardinality\":\"n\",\"datatype\":\"string\",\"field\":\"fields\",\"phase\":\"alpha\",\"register\":\"field\",\"text\":\"Set of field names.\"}"));
        TestDBItem expectedItem7 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "f404b4739b51afeb39bba26f3bbf1aa8c6f7d25f0d54444992fc00f24587ef77"),
                nodeOf("{\"fields\":[\"register\",\"text\",\"registry\",\"phase\",\"copyright\",\"fields\"],\"phase\":\"alpha\",\"register\":\"register\",\"registry\":\"cabinet-office\",\"text\":\"Register of registers\"}"));
        TestDBItem expectedItem8 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test\"}"));
        TestDBItem expectedItem9 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test2\"}"));


        List<TestDBItem> storedItems = testItemDAO.getItems(schema);
        assertThat(storedItems, containsInAnyOrder(expectedItem1, expectedItem2, expectedItem3, expectedItem4, expectedItem5, expectedItem6, expectedItem7, expectedItem8, expectedItem9));

        List<Entry> systemEntries = testEntryDAO.getAllSystemEntries(schema);
        assertThat(systemEntries.get(0).getEntryNumber(), is(1));
        assertThat(systemEntries.get(0).getItemHashes().get(0).getValue(), is("955a84bcec7dad1a4d9b05e28ebfa21b17ac9552cc0aabbc459c73d63ab530b0"));
        assertThat(systemEntries.get(1).getEntryNumber(), is(2));
        assertThat(systemEntries.get(1).getItemHashes().get(0).getValue(), is("ceae38992b310fba3ae77fd84e21cdb6838c90b36bcb558de02acd2f6589bd3f"));
        assertThat(systemEntries.get(2).getEntryNumber(), is(3));
        assertThat(systemEntries.get(2).getItemHashes().get(0).getValue(), is("4624c413d90e125141a92f28c9ea4300a568d9b5d9c1c7ad13623433c4a370f2"));
        assertThat(systemEntries.get(3).getEntryNumber(), is(4));
        assertThat(systemEntries.get(3).getItemHashes().get(0).getValue(), is("1c5a799079c97f1dcea1b244d9962b0de248ba1282145c2e815839815db1d0a4"));
        assertThat(systemEntries.get(4).getEntryNumber(), is(5));
        assertThat(systemEntries.get(4).getItemHashes().get(0).getValue(), is("c7e5a90c020f7686d9a275cb0cc164636745b10ae168a72538772692cc90d633"));
        assertThat(systemEntries.get(5).getEntryNumber(), is(6));
        assertThat(systemEntries.get(5).getItemHashes().get(0).getValue(), is("61138002a7ae8a53f3ad16bb91ee41fe73cc7ab7c8b24a8afd2569eb0e6a1c26"));
        assertThat(systemEntries.get(6).getEntryNumber(), is(7));
        assertThat(systemEntries.get(6).getItemHashes().get(0).getValue(), is("f404b4739b51afeb39bba26f3bbf1aa8c6f7d25f0d54444992fc00f24587ef77"));

        List<Entry> userEntries = testEntryDAO.getAllEntries(schema);

        assertThat(userEntries.get(0).getEntryNumber(), is(1));
        assertThat(userEntries.get(0).getItemHashes().get(0).getValue(), is("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));
        assertThat(userEntries.get(1).getEntryNumber(), is(2));
        assertThat(userEntries.get(1).getItemHashes().get(0).getValue(), is("b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"));

        Record record1 = testRecordDAO.findRecords(Arrays.asList("ft_openregister_test"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record1.getEntry().getEntryNumber(), equalTo(1));
        assertThat(record1.getEntry().getKey(), equalTo("ft_openregister_test"));
        Record record2 = testRecordDAO.findRecords(Arrays.asList("ft_openregister_test2"), IndexNames.RECORD, schema, "entry").get(0);
        assertThat(record2.getEntry().getEntryNumber(), equalTo(2));
        assertThat(record2.getEntry().getKey(), equalTo("ft_openregister_test2"));
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
    public void shouldReturnBadRequestForRegisterDefinitionWhenBeforeFieldDefinitions() throws IOException {
        String input = new String(RsfRegisterDefinition.ADDRESS_REGISTER + RsfRegisterDefinition.ADDRESS_FIELDS);

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, register:address, 2017-06-06T09:54:11Z, sha-256:8d824e2afa57f1a71980237341b0c75d61fdc5c32e52d91e64c6fc3c6265ae63]}\",\"details\":\"Field undefined: register - address\"}"));
    }
    
    @Test
    public void shouldAllowRegisterTextToBeUpdatedAfterUserEntry() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-by-registry-valid-entry-ordering.rsf")));
        
        Response r = send(input);
        
        assertThat(r.getStatus(), equalTo(200));
        Response response = register.getRequest(testRegister, "/register.json");
        Map registerResourceMapFromRegisterRegister = response.readEntity(Map.class);
        Map<?, ?> registerRecordMapFromRegisterRegister = (Map) registerResourceMapFromRegisterRegister.get("register-record");
        assertThat(registerRecordMapFromRegisterRegister.get("text"), equalTo("Register of registers X"));
    }

    @Test
    public void shouldNotAllowRegisterNonTextValuesToBeUpdatedAfterUserEntry() throws IOException {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-by-registry-invalid-entry-ordering.rsf")));

        Response r = send(input);

        assertThat(r.getStatus(), equalTo(400));
        assertThat(r.readEntity(String.class), containsString("Definition of register register does not match Register Register"));
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

	@Test
	public void shouldReturnBadRequestWhenRegisterIsMissingFieldsDefinedInEnvironment() throws Exception {
		Response response = register.loadRsf(TestRegister.postcode,
				"add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
						"append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a\n" +
						"add-item\t{\"fields\":[\"postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
						"append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n");
		assertThat(response.getStatus(), equalTo(400));
		assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:postcode, 2017-06-09T12:59:51Z, sha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a]}\",\"details\":\"Definition of field postcode does not match Field Register\"}"));
	}
    
    @Test
    public void shouldReturnBadRequestWhenLocalFieldDoesNotExistInEnvironment() throws Exception {
        Response response = register.loadRsf(TestRegister.postcode,
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"test-postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
            "append-entry\tsystem\tfield:test-postcode\t2017-06-09T12:59:51Z\tsha-256:eb0381c0c768767e60b3edf140e6bdf241f5e6f01a98c3751da488c3e6ffb3fe\n" +
            "add-item\t{\"fields\":[\"test-postcode\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
            "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:323fb3d9167d55ea8173172d756ddbc653292f8debbb13f251f7057d5cb5e450\n");
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:test-postcode, 2017-06-09T12:59:51Z, sha-256:eb0381c0c768767e60b3edf140e6bdf241f5e6f01a98c3751da488c3e6ffb3fe]}\",\"details\":\"Field test-postcode does not exist in Field Register\"}"));
    }

    @Test
    public void shouldReturnBadRequestWhenLocalFieldDoesNotMatchEnvironmentField() throws Exception {
        String rsf =
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"postcode\",\"phase\":\"alpha\",\"text\":\"UK Postcodes.\"}\n" +
            "add-item\t{\"cardinality\":\"1\",\"datatype\":\"string\",\"field\":\"point\",\"phase\":\"alpha\",\"text\":\"A geographical point\"}\n" +
            "append-entry\tsystem\tfield:postcode\t2017-06-09T12:59:51Z\tsha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a\n" +
            "append-entry\tsystem\tfield:point\t2017-06-09T12:59:51Z\tsha-256:48d0ad5afa2502674a2253c62e5af3f9bc10f5c6fbc5d16784c9dcfbc60d066b\n" +
            "add-item\t{\"fields\":[\"postcode\",\"point\"],\"phase\":\"alpha\",\"register\":\"test\",\"registry\":\"cabinet-office\",\"text\":\"Register of postcodes\"}\n" +
            "append-entry\tsystem\tregister:postcode\t2017-06-06T09:54:11Z\tsha-256:ee2fd6546a8362d98e3cd63d914ca55d93f15801c35fdd108b9294c4f0a1d01e\n";

        Response response = register.loadRsf(TestRegister.postcode, rsf);
        assertThat(response.getStatus(), equalTo(400));
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Exception when executing command: RegisterCommand{commandName='append-entry', arguments=[system, field:postcode, 2017-06-09T12:59:51Z, sha-256:689e7a836844817b102d0049c6d402fc630f1c9f284ee96d9b7ec24bc7e0c36a]}\",\"details\":\"Definition of field postcode does not match Field Register\"}"));
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
