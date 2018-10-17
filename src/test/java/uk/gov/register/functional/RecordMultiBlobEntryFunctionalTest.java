package uk.gov.register.functional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.rules.TestRule;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;
import uk.gov.register.functional.app.TestRegister;
import uk.gov.register.functional.app.WipeDatabaseRule;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.register.functional.app.TestRegister.address;

public class RecordMultiBlobEntryFunctionalTest {

    private static final TestRegister testRegister = TestRegister.register;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Rule
    public TestRule wipe = new WipeDatabaseRule();

    @ClassRule
    public static final RegisterRule register = new RegisterRule();

    @BeforeClass
    public static void setup() throws IOException {
        System.setProperty("multi-item-entries-enabled", "true");
        String payload = new String(Files.readAllBytes(Paths.get("src/test/resources/fixtures/serialized", "register-by-registry.rsf")));
        register.loadRsf(testRegister, RsfRegisterDefinition.REGISTER_REGISTER);
        register.loadRsf(testRegister, payload);
    }

    @AfterClass
    public static void clearMultiItemsEnabled() {
        System.clearProperty("multi-item-entries-enabled");
    }

    @Test
    public void shouldNotIncludeRecordsWithNoItems() throws IOException {
        Response response = register.getRequest(testRegister, "/records.json");
        assertThat(response.getStatus(), equalTo(200));
        Map<String, Object> res = MAPPER.readValue(response.readEntity(String.class), Map.class);
        assertTrue(res.containsKey("government-digital-service"));
        assertFalse(res.containsKey("cabinet-office"));
    }

    @Test
    public void shouldNotFindRecordWithNoItems() {
        Response response = register.getRequest(testRegister, "/record/cabinet-office.json");
        assertThat(response.getStatus(), equalTo(404));
    }

    @Test
    public void shouldShowEntriesForRecordWithNoItems() throws IOException {
        Response response = register.getRequest(testRegister, "/record/cabinet-office/entries.json");
        assertThat(response.getStatus(), equalTo(200));
        ArrayNode res = MAPPER.readValue(response.readEntity(String.class), ArrayNode.class);
        assertThat(res.size(), is(1));
        ArrayNode hashes = (ArrayNode) res.get(0).get("item-hash");
        assertThat(hashes.size(), is(0));
    }

    @Test
    public void shouldRenderListOfItems() throws IOException {
        Response response = register.getRequest(testRegister, "/record/government-digital-service.json");
        assertThat(response.getStatus(), equalTo(200));
        JsonNode res = MAPPER.readValue(response.readEntity(String.class), JsonNode.class).get("government-digital-service");
        ArrayNode items = (ArrayNode) res.get("item");
        assertThat(items.size(), Matchers.is(2));
    }

}
