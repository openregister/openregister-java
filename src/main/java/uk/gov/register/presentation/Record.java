package uk.gov.register.presentation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import uk.gov.register.presentation.config.FieldsConfiguration;
import uk.gov.register.presentation.mapper.JsonObjectMapper;

import java.util.Map;

public class Record {
    private final String hash;
    private final JsonNode jsonEntry;
    private FieldsConfiguration fieldsConfiguration;

    @JsonCreator
    public Record(@JsonProperty("hash") String hash, @JsonProperty("entry") JsonNode jsonEntry) {
        this.hash = hash;
        this.jsonEntry = jsonEntry;
    }

    public void setFieldsConfiguration(FieldsConfiguration fieldsConfiguration) {
        this.fieldsConfiguration = fieldsConfiguration;
    }

    @JsonProperty
    public String getHash() {
        return hash;
    }


    @JsonProperty("entry")
    public Map<String, Object> getEntry() {
        return JsonObjectMapper.convert(jsonEntry, new TypeReference<Map<String, Object>>() {
        });
    }

    //methods below are basically for representation purpose

    public String registerEntryLink(String fieldName) {
        return String.format("http://%1$s.openregister.org/%1$s/%2$s", fieldName, getEntry().get(fieldName));
    }

    @SuppressWarnings("unused, used from htmlview")
    public boolean hasRegister(String fieldName) {
        return fieldsConfiguration.getField(fieldName).getRegister().isPresent();
    }
}
