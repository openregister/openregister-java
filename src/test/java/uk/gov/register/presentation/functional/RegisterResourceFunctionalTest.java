package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Ignore;
import org.junit.Test;
import uk.gov.register.presentation.config.ResourceYamlFileReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.register.presentation.functional.TestEntry.anEntry;


public class RegisterResourceFunctionalTest extends FunctionalTestBase {

    @Test
    @Ignore("Needs to update registers.yaml with new records resource")
    public void registerJsonShouldContainEntryView() throws Throwable {
        populateRegisterRegisterEntries();
        Response registerRegisterEntryResponse = getRequest("register", "/record/address.json");
        assertThat(registerRegisterEntryResponse.getStatus(), equalTo(200));

        resetSchema();

        populateAddressRegisterEntries();

        Response addressRegisterResourceResponse = getRequest("address", "/register.json");
        assertThat(addressRegisterResourceResponse.getStatus(), equalTo(200));

        JsonNode registerJson = addressRegisterResourceResponse.readEntity(JsonNode.class);

        assertThat(registerJson.get("total-entries").intValue(), equalTo(5));
        assertThat(registerJson.get("total-records").intValue(), equalTo(3));
        assertThat(registerJson.get("total-items").intValue(), equalTo(5));
        verifyStringIsAnISODate(registerJson.get("last-updated").textValue());

        JsonNode registerRecordJson = registerRegisterEntryResponse.readEntity(JsonNode.class);
        assertThat(registerJson.get("record"), equalTo(registerRecordJson));
    }

    public void populateAddressRegisterEntries() {
        dbSupport.publishEntries(ImmutableList.of(
                anEntry(1, "{\"name\":\"ellis\",\"address\":\"12345\"}"),
                anEntry(1, "{\"name\":\"presley\",\"address\":\"6789\"}"),
                anEntry(1, "{\"name\":\"ellis\",\"address\":\"145678\"}"),
                anEntry(1, "{\"name\":\"updatedEllisName\",\"address\":\"145678\"}"),
                anEntry(1, "{\"name\":\"ellis\",\"address\":\"6789\"}")
        ));
    }

    public void populateRegisterRegisterEntries() throws IOException {
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
