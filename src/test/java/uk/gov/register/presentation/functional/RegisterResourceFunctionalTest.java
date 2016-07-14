package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.configuration.ResourceYamlFileReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static uk.gov.register.presentation.functional.TestEntry.anEntry;


public class RegisterResourceFunctionalTest extends FunctionalTestBase {

    private final Map<?,?> expectedAddressRegisterMap = getAddressRegisterMap();

    @Test
    public void registerJsonShouldContainEntryViewAddressRegister() throws Throwable {
        populateRegisterRegisterEntries();
        Response addressRecordInRegisterRegisterResponse = getRequest("register", "/record/address.json");
        assertThat(addressRecordInRegisterRegisterResponse.getStatus(), equalTo(200));
        Map<?,?> addressRecordMapInRegisterRegister = addressRecordInRegisterRegisterResponse.readEntity(Map.class);
        verifyStringIsAnISODate(addressRecordMapInRegisterRegister.get("entry-timestamp").toString());

        assertAddressRegisterMapIsEqualTo(addressRecordMapInRegisterRegister);
    }

    @Test
    public void registerJsonShouldContainEntryViewRegisterRegister() throws Throwable {
        populateAddressRegisterEntries();

        Response registerResourceFromAddressRegisterResponse = getRequest("address", "/register.json");
        assertThat(registerResourceFromAddressRegisterResponse.getStatus(), equalTo(200));

        Map registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(Map.class);

        assertThat(registerResourceMapFromAddressRegister.get("total-entries"), equalTo(5));
        assertThat(registerResourceMapFromAddressRegister.get("total-records"), equalTo(3));
        assertThat(registerResourceMapFromAddressRegister.get("total-items"), equalTo(5));
        verifyStringIsAnISODate(registerResourceMapFromAddressRegister.get("last-updated").toString());

        Map<?,?> registerRecordMapFromAddressRegister = (Map)registerResourceMapFromAddressRegister.get("register-record");
        verifyStringIsAnISODate(registerRecordMapFromAddressRegister.get("entry-timestamp").toString());

        assertAddressRegisterMapIsEqualTo(registerRecordMapFromAddressRegister);
    }

    private void populateAddressRegisterEntries() {
        dbSupport.publishEntries(ImmutableList.of(
                anEntry(1, "{\"name\":\"ellis\",\"address\":\"12345\"}"),
                anEntry(2, "{\"name\":\"presley\",\"address\":\"6789\"}"),
                anEntry(3, "{\"name\":\"ellis\",\"address\":\"145678\"}"),
                anEntry(4, "{\"name\":\"updatedEllisName\",\"address\":\"145678\"}"),
                anEntry(5, "{\"name\":\"ellis\",\"address\":\"6789\"}")
        ));
    }

    private void populateRegisterRegisterEntries() throws IOException {
        Collection<Map> registers = new ResourceYamlFileReader().readResource(
                Optional.empty(),
                "config/registers.yaml",
                new TypeReference<Map<String, Map>>() {
                }
        );

        List<TestEntry> registerEntries = registers.stream().map(r -> {
            int entryNumber = Integer.parseInt(r.remove("entry-number").toString());
            String timestampISO = (String) r.remove("entry-timestamp");
            r.remove("item-hash");
            return anEntry(entryNumber, writeToString(r), Instant.parse(timestampISO));
        }).collect(Collectors.toList());

        dbSupport.publishEntries("register", registerEntries);
    }

    private void assertAddressRegisterMapIsEqualTo(Map<?, ?> sutAddressRecordMapInRegisterRegister) {
        for (Map.Entry entry : expectedAddressRegisterMap.entrySet()) {
            assertThat(sutAddressRecordMapInRegisterRegister, hasEntry(entry.getKey(), entry.getValue()));
        }
    }

    private String writeToString(Map map) {
        try {
            return Jackson.newObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw Throwables.propagate(e);
        }
    }

    private void verifyStringIsAnISODate(String lastUpdated) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
        TemporalAccessor parsedDate = isoFormatter.parse(lastUpdated);
        assertThat(isoFormatter.format(parsedDate), equalTo(lastUpdated));
    }

    private HashMap<String, Object> getAddressRegisterMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("entry-number", "1");
        result.put("item-hash", "sha-256:6a7bcb516b4a465f3340a31ab42a35a9193e512608d9577388da4d99251880a8");
        result.put("entry-timestamp", "2016-04-21T10:14:21Z");
        result.put("text", "Postal addresses in the UK");
        result.put("phase", "alpha");
        result.put("fields", Arrays.asList("address", "property", "street", "locality", "town", "area", "postcode", "country", "latitude", "longitude"));
        result.put("register", "address");
        result.put("registry", "office-for-national-statistics");
        result.put("copyright", "Contains Ordnance Survey data © Crown copyright & database right 2015\n Contains Royal Mail data © Royal Mail copyright & database right 2015\n");
        return result;
    }
}
