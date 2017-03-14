package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.skife.jdbi.v2.Handle;
import uk.gov.register.core.Entry;
import uk.gov.register.core.HashingAlgorithm;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.app.WipeDatabaseRule;
import uk.gov.register.functional.db.TestDBItem;
import uk.gov.register.functional.db.TestEntryDAO;
import uk.gov.register.functional.db.TestItemCommandDAO;
import uk.gov.register.functional.db.TestRecord;
import uk.gov.register.functional.db.TestRecordDAO;
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

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @ClassRule
    public static final RegisterRule register = new RegisterRule();
    private static TestItemCommandDAO testItemDAO;
    private static TestEntryDAO testEntryDAO;
    private static TestRecordDAO testRecordDAO;

    @BeforeClass
    public static void setUp() throws Exception {
        Handle handle = register.handleFor(testRegister);
        testItemDAO = handle.attach(TestItemCommandDAO.class);
        testEntryDAO = handle.attach(TestEntryDAO.class);
        testRecordDAO = handle.attach(TestRecordDAO.class);
    }

    @Test
    public void checkMessageIsConsumedAndStoredInDatabase() throws Exception {
        String input = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-register-rsf.tsv")));
        Response r = send(input);

        assertThat(r.getStatus(), equalTo(200));

        TestDBItem expectedItem1 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test\"}"));
        TestDBItem expectedItem2 = new TestDBItem(
                new HashValue(HashingAlgorithm.SHA256, "b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"),
                nodeOf("{\"text\":\"SomeText\",\"register\":\"ft_openregister_test2\"}"));


        List<TestDBItem> storedItems = testItemDAO.getItems();
        assertThat(storedItems, containsInAnyOrder(expectedItem1, expectedItem2));

        List<Entry> entries = testEntryDAO.getAllEntries();
        assertThat(entries.get(0).getEntryNumber(), is(1));
        assertThat(entries.get(0).getItemHashes().get(0).getValue(), is("3cee6dfc567f2157208edc4a0ef9c1b417302bad69ee06b3e96f80988b37f254"));
        assertThat(entries.get(1).getEntryNumber(), is(2));
        assertThat(entries.get(1).getItemHashes().get(0).getValue(), is("b8b56d0329b4a82ce55217cfbb3803c322bf43711f82649757e9c2df5f5b8371"));

        TestRecord record1 = testRecordDAO.getRecord("ft_openregister_test");
        assertThat(record1.getEntryNumber(), equalTo(1));
        assertThat(record1.getPrimaryKey(), equalTo("ft_openregister_test"));
        TestRecord record2 = testRecordDAO.getRecord("ft_openregister_test2");
        assertThat(record2.getEntryNumber(), equalTo(2));
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
        assertThat(response.readEntity(String.class), equalTo("{\"success\":false,\"message\":\"Orphan add item (line:3): sha-256:d00d4b610e9b5af160a7e5e836eec9e12626cac61823eda1c3ec9a59a78eefaa\"}"));
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
        assertThat(testItemDAO.getItems(), is(empty()));
        assertThat(testEntryDAO.getAllEntries(), is(empty()));
    }

    private Response send(String payload) {
        return register.loadRsf(testRegister, payload);
    }

    private JsonNode nodeOf(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, JsonNode.class);
    }
}
