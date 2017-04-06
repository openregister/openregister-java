package uk.gov.register.functional;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import uk.gov.register.functional.app.RegisterRule;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.register.functional.app.TestRegister.address;

public class RegisterResourceFunctionalTest {

    @ClassRule
    public static RegisterRule register = new RegisterRule();

    @Before
    public void setup() {
        register.wipe();
    }
    private final Map<?,?> expectedAddressRegisterMap = getAddressRegisterMap();

    @Test
    public void registerJsonShouldContainEntryViewRegisterRegister() throws Throwable {
        register.mintLines(address, "{\"address\":\"12345\"}",
                "{\"address\":\"6789\"}",
                "{\"address\":\"145678\"}",
                "{\"address\":\"145678\"}",
                "{\"address\":\"6789\"}");

        Response registerResourceFromAddressRegisterResponse = register.getRequest(address, "/register.json");
        assertThat(registerResourceFromAddressRegisterResponse.getStatus(), equalTo(200));

        Map registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(Map.class);

        assertThat(registerResourceMapFromAddressRegister.get("total-entries"), equalTo(5));
        assertThat(registerResourceMapFromAddressRegister.get("total-records"), equalTo(3));
        verifyStringIsAnISODate(registerResourceMapFromAddressRegister.get("last-updated").toString());

        Map<?,?> registerRecordMapFromAddressRegister = (Map)registerResourceMapFromAddressRegister.get("register-record");
        verifyStringIsAnISODate(registerRecordMapFromAddressRegister.get("entry-timestamp").toString());

        assertAddressRegisterMapIsEqualTo(registerRecordMapFromAddressRegister);
    }

    @Test
    public void registerJsonShouldGenerateValidResponseForEmptyDB(){
        Response registerResourceFromAddressRegisterResponse = register.getRequest(address, "/register.json");
        assertThat(registerResourceFromAddressRegisterResponse.getStatus(), equalTo(200));

        Map<String,?> registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(new GenericType<Map<String, ?>>(){});

        assertThat(registerResourceMapFromAddressRegister.get("total-entries"), equalTo(0));
        assertThat(registerResourceMapFromAddressRegister.get("total-records"), equalTo(0));

        assertThat(registerResourceMapFromAddressRegister, not(hasKey("last-updated")));
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
        result.put("index-entry-number", "1");
        result.put("entry-number", "1");
        result.put("entry-timestamp", "2016-04-21T10:14:21Z");
        result.put("key", "address");
        result.put("text", "Postal addresses in the UK");
        result.put("phase", "alpha");
        result.put("fields", Arrays.asList("address", "property", "street", "locality", "town", "area", "postcode", "country", "latitude", "longitude"));
        result.put("register", "address");
        result.put("registry", "office-for-national-statistics");
        result.put("copyright", "Contains Ordnance Survey data © Crown copyright & database right 2015\n Contains Royal Mail data © Royal Mail copyright & database right 2015\n");
        return result;
    }
}
