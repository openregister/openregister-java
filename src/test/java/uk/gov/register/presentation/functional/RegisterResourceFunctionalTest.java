package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.presentation.config.ResourceYamlFileReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.presentation.functional.TestEntry.anEntry;


public class RegisterResourceFunctionalTest extends FunctionalTestBase {

    @Test
    public void registerJsonShouldContainEntryView() throws Throwable {
        populateRegisterRegisterEntries();
        Response addressRecordInRegisterRegisterResponse = getRequest("register", "/record/address.json");
        assertThat(addressRecordInRegisterRegisterResponse.getStatus(), equalTo(200));
        Map addressRecordMapInRegisterRegister = addressRecordInRegisterRegisterResponse.readEntity(Map.class);
        verifyStringIsAnISODate(addressRecordMapInRegisterRegister.remove("entry-timestamp").toString());

        resetSchema();

        populateAddressRegisterEntries();

        Response registerResourceFromAddressRegisterResponse = getRequest("address", "/register.json");
        assertThat(registerResourceFromAddressRegisterResponse.getStatus(), equalTo(200));

        Map registerResourceMapFromAddressRegister = registerResourceFromAddressRegisterResponse.readEntity(Map.class);

        assertThat(registerResourceMapFromAddressRegister.get("total-entries"), equalTo(5));
        assertThat(registerResourceMapFromAddressRegister.get("total-records"), equalTo(3));
        assertThat(registerResourceMapFromAddressRegister.get("total-items"), equalTo(5));
        verifyStringIsAnISODate(registerResourceMapFromAddressRegister.get("last-updated").toString());

        Map registerRecordMapFromAddressRegister = (Map)registerResourceMapFromAddressRegister.get("register-record");
        verifyStringIsAnISODate(registerRecordMapFromAddressRegister.remove("entry-timestamp").toString());

        assertThat(registerRecordMapFromAddressRegister.toString(), equalTo(addressRecordMapInRegisterRegister.toString()));
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
            r.remove("entry-timestamp");
            r.remove("item-hash");
            return anEntry(entryNumber, writeToString(r));
        }).collect(Collectors.toList());

        dbSupport.publishEntries("register", registerEntries);
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
}
