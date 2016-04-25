package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.dropwizard.jackson.Jackson;
import uk.gov.register.presentation.config.Register;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegisterData {
    final ObjectMapper yamlObjectMapper = Jackson.newObjectMapper(new YAMLFactory());

    final String entryNumber;
    final String itemHash;
    final String entryTimestamp;
    final Map<String, JsonNode> data;

    @JsonCreator
    public RegisterData(Map<String, JsonNode> data) {
        this.entryNumber = data.remove("entry-number").textValue();
        this.itemHash = data.remove("item-hash").textValue();
        this.entryTimestamp = data.remove("entry-timestamp").textValue();
        this.data = data;
    }

    public Register getRegister() {
        return yamlObjectMapper.convertValue(data, Register.class);
    }

    public EntryView getEntry(EntryConverter entryConverter) {
        return entryConverter.convert(Integer.parseInt(entryNumber), itemHash, data.entrySet());
    }
}
