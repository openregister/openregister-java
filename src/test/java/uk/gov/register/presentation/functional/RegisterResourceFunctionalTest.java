package uk.gov.register.presentation.functional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import uk.gov.register.presentation.functional.testSupport.DBSupport;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsString;
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

        String addressRegisterResourceJsonResponse = addressRegisterResourceResponse.readEntity(String.class);
        assertThat(addressRegisterResourceJsonResponse, containsString("\"total-entries\":5"));
        assertThat(addressRegisterResourceJsonResponse, containsString("\"total-records\":3"));
        assertThat(addressRegisterResourceJsonResponse, containsString("\"total-items\":5"));

        String registerRegisterEntryJsonResponse = registerRegisterEntryResponse.readEntity(String.class);
        assertThat(addressRegisterResourceJsonResponse, containsString(registerRegisterEntryJsonResponse));
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
        ObjectMapper jsonObjectMapper = Jackson.newObjectMapper();

        List<Map<String, JsonNode>> registersYaml = yamlObjectMapper.readValue(registersStream, new TypeReference<List<Map<String, JsonNode>>>() {
        });

        Map<Integer, String> registerEntries = registersYaml.stream().collect(Collectors.toMap(m -> m.get("serial-number").asInt(), m -> {
            try {
                return jsonObjectMapper.writeValueAsString(yamlObjectMapper.convertValue(m, ImportRegister.class));
            } catch (JsonProcessingException e) {
                throw new UncheckedIOException(e);
            }
        }));

        DBSupport.publishMessages("register", registerEntries);
    }

    @JsonIgnoreProperties("serial-number")
    private static class ImportRegister {
        public String hash;
        public JsonNode entry;

        public ImportRegister() {}
    }
}
