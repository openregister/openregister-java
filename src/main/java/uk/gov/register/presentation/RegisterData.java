package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.config.Register;

import java.util.Map;

public class RegisterData {
    final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    final int serialNumber;
    final String hash;
    final String timestamp;
    final Map<String, JsonNode> data;

    @JsonCreator
    public RegisterData(
            @JsonProperty("serial-number") int serialNumber,
            @JsonProperty("hash") String hash,
            @JsonProperty("last-updated") String timestamp,
            @JsonProperty("entry") Map<String, JsonNode> data) {
        this.serialNumber = serialNumber;
        this.hash = hash;
        this.timestamp = timestamp;
        this.data = data;
    }

    public Register getRegister() {
        return  yamlObjectMapper.convertValue(data, Register.class);
    }

    public EntryView getEntry(EntryConverter entryConverter) {
        return entryConverter.convert(serialNumber, hash, data.entrySet());
    }
}
