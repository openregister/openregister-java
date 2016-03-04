package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;


public class RegisterResourceFunctionalTest extends FunctionalTestBase {

    @Test
    public void registerJsonShouldContainEntryView() throws Throwable {
        populateRegisterRegisterEntries();
        Response registerRegisterEntryResponse = getRequest("register", "/register/address.json");
        assertThat(registerRegisterEntryResponse.getStatus(), equalTo(200));

        cleanDatabaseRule.before();

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
        DBSupport.publishMessages(ImmutableList.of(
                "{\"hash\":\"hash1\",\"entry\":{\"name\":\"ellis\",\"address\":\"12345\"}}",
                "{\"hash\":\"hash2\",\"entry\":{\"name\":\"presley\",\"address\":\"6789\"}}",
                "{\"hash\":\"hash3\",\"entry\":{\"name\":\"ellis\",\"address\":\"145678\"}}",
                "{\"hash\":\"hash4\",\"entry\":{\"name\":\"updatedEllisName\",\"address\":\"145678\"}}",
                "{\"hash\":\"hash5\",\"entry\":{\"name\":\"ellis\",\"address\":\"6789\"}}"
        ));
    }

    public void populateRegisterRegisterEntries() throws IOException {
        InputStream registersStream = RegisterResourceFunctionalTest.class.getClassLoader().getResourceAsStream("config/registers.yaml");
        ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());
        List<Map<String, JsonNode>> registersYaml = yamlObjectMapper.readValue(registersStream, new TypeReference<List<Map<String, JsonNode>>>() {
        });

        SortedMap<Integer, String> registerEntries = registersYaml.stream().collect(Collectors.toMap(m -> m.get("serial-number").asInt(),
                m -> {
                    ObjectNode rootNode = yamlObjectMapper.createObjectNode();
                    rootNode.set("hash", m.get("hash"));
                    rootNode.set("entry", m.get("entry"));
                    return rootNode.toString();
                }, (a, b) -> a, TreeMap::new));

        DBSupport.publishMessages("register", registerEntries);
    }

    private void verifyStringIsAnISODate(String lastUpdated) {
        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_INSTANT;
        TemporalAccessor parsedDate = isoFormatter.parse(lastUpdated);
        assertThat(isoFormatter.format(parsedDate), equalTo(lastUpdated));
    }
}
