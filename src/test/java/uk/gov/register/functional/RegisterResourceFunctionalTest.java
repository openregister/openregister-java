package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;
import uk.gov.register.functional.app.RsfRegisterDefinition;

import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static uk.gov.register.functional.app.TestRegister.address;
import static uk.gov.register.functional.app.TestRegister.postcode;

public class RegisterResourceFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.wipe();
        register.loadRsfV1(address, RsfRegisterDefinition.ADDRESS_FIELDS +  RsfRegisterDefinition.ADDRESS_REGISTER );
    }

    private final Map<?, ?> expectedAddressRegisterMap = getAddressRegisterMap();

    @Test
    public void registerJsonShouldContainEntryViewRegisterRegister() throws Throwable {

        String payload = "add-item\t{\"custodian\":\"Stephen McAllister\"}\n" +
                "append-entry\tsystem\tcustodian\t2017-01-10T17:16:07Z\tsha-256:1a79573f28a473bc281ae5ef7f1a910da710648bc666abf5930e05e1ed962f39\n" +
                "add-item\t{\"address\":\"12345\"}\n" +
                "append-entry\tuser\t12345\t2017-05-23T10:12:34Z\tsha-256:5a850dd38262ddc5c4b25532215fea767573e4cd9cb0130b36548fd04b34518d\n" +
                "add-item\t{\"address\":\"6789\"}\n" +
                "append-entry\tuser\t6789\t2017-05-23T10:12:34Z\tsha-256:f715806d5b3a85ee3593f53653eb274188cf02de00d4a2064a2c8952617de7fe\n" +
                "add-item\t{\"address\":\"145678\"}\n" +
                "append-entry\tuser\t145678\t2017-05-23T10:12:34Z\tsha-256:921c14161f7c13a18a52e8418c0c69ac7211d40cbaf53c58513dc668d68376d8";

        register.loadRsfV1(address, payload);

        Response registerResourceFromAddressRegisterResponse = register.getRequest(address, "/register.json");
        assertThat(registerResourceFromAddressRegisterResponse.getStatus(), equalTo(200));

        Map registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(Map.class);

        assertThat(registerResourceMapFromAddressRegister.get("total-entries"), equalTo(3));
        assertThat(registerResourceMapFromAddressRegister.get("total-records"), equalTo(3));
        assertThat(registerResourceMapFromAddressRegister.get("custodian"), equalTo("Stephen McAllister"));
        verifyStringIsAnISODate(registerResourceMapFromAddressRegister.get("last-updated").toString());

        Map<?, ?> registerRecordMapFromAddressRegister = (Map) registerResourceMapFromAddressRegister.get("register-record");

        assertAddressRegisterMapIsEqualTo(registerRecordMapFromAddressRegister);
    }

    @Test
    public void registerJsonShouldContainCorrectFieldsForRegister() {
        register.loadRsfV1(address, RsfRegisterDefinition.ADDRESS_REGISTER);
        Response registerResourceFromAddressRegisterResponse = register.getRequest(address, "/register.json");
        Map registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(Map.class);
        Map<?, ?> registerRecordMapFromAddressRegister = (Map) registerResourceMapFromAddressRegister.get("register-record");

        assertThat(registerRecordMapFromAddressRegister.get("text"), equalTo("Register of addresses"));
        assertThat((List<String>) registerRecordMapFromAddressRegister.get("fields"), containsInAnyOrder("address", "street", "locality", "town", "area", "postcode", "country", "latitude", "longitude", "property"));
    }

    @Test
    public void registerJsonShouldShowExceptionWhenRegisterMetadataIsUndefined() {
        Response response = register.getRequest(postcode, "/register.json");

        assertThat(response.getStatus(), equalTo(200));
        assertThat(response.readEntity(String.class), containsString("Register undefined"));
    }

    private void assertAddressRegisterMapIsEqualTo(Map<?, ?> sutAddressRecordMapInRegisterRegister) {
        for (Map.Entry entry : expectedAddressRegisterMap.entrySet()) {
            assertThat(sutAddressRecordMapInRegisterRegister, hasEntry(entry.getKey(), entry.getValue()));
        }
    }

    private void verifyStringIsAnISODate(String lastUpdated) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
        TemporalAccessor parsedDate = isoFormatter.parse(lastUpdated);
        assertThat(isoFormatter.format(parsedDate), equalTo(lastUpdated));
    }

    private HashMap<String, Object> getAddressRegisterMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("text", "Register of addresses");
        result.put("phase", "alpha");
        result.put("fields", Arrays.asList("address","street","locality","town","area","postcode","country","latitude","longitude","property"));
        result.put("register", "address");
        result.put("registry", "office-for-national-statistics");
        return result;
    }
}
