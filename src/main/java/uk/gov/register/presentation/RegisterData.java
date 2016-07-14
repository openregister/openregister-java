package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.configuration.Register;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterData {
    private final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    private final Map<String, JsonNode> data;

    @JsonCreator
    public RegisterData(Map<String, JsonNode> data) {
        this.data = data;
    }

    public Register getRegister() {
        return yamlObjectMapper.convertValue(data, Register.class);
    }

    @SuppressWarnings("unused, used to serialize in register json")
    @JsonValue
    private Map<String, JsonNode> getRegisterData() {
        return data;
    }
}
